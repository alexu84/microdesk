# Microdesk

A microservices-based office desk booking system built with Java 21, Spring Boot, PostgreSQL, and Kafka. Features JWT authentication, secure REST APIs, and a resilient idempotent notification service deployed via Docker Compose.

---

## System Design

### Architecture overview

Microdesk follows a microservices architecture. The two services are independent Spring Boot applications that communicate asynchronously through a Kafka topic. Each service owns its own PostgreSQL database — they share no schema and no direct database connection.

```
 Client (REST)
      │
      ▼
┌─────────────────┐        Kafka topic          ┌──────────────────────┐
│  booking-service │  ──── reservations ────►  │ notification-service  │
│   (port 8081)   │                             │     (port 8082)       │
└────────┬────────┘                             └──────────┬───────────┘
         │                                                 │
         ▼                                                 ▼
   booking DB (PG)                                 notification DB (PG)
```

### booking-service

Exposes a REST API for:
- **Auth** — register and login with email/password. Passwords are bcrypt-hashed. Successful login returns a signed JWT used to authenticate all subsequent requests.
- **Desks** — read available desks for a given date.
- **Reservations** — create and cancel desk reservations, view own reservation history.

Business rules enforced:
- A user can only have one active reservation per day.
- A desk can only be booked once per day.
- Only future reservations can be cancelled.
- Only the owner of a reservation can cancel it.

**Transactional outbox pattern** — when a reservation is created or cancelled, the service writes a serialised event to an `outbox_messages` table in the same transaction as the reservation. A background scheduler polls the outbox and forwards pending messages to Kafka, then marks them as sent. This decouples the business transaction from Kafka availability and prevents message loss.

### notification-service

Consumes reservation events from Kafka and persists them as notifications scoped to the user. The notifications endpoint in the booking-service proxies reads to this service.

**Idempotent consumer** — before saving, the service checks whether a notification for that `(reservationId, eventType)` pair already exists. A unique database constraint provides a second line of defence against concurrent duplicates. If a duplicate is detected at either layer the message is silently skipped. If processing fails the Kafka offset is not acknowledged, so the broker redelivers the message.

### Infrastructure

All components are orchestrated with Docker Compose:

| Container | Image | Port |
|-----------|-------|------|
| `microdesk-postgres` | postgres:18-alpine | 5435 |
| `kafka` | apache/kafka:3.7.0 (KRaft mode) | 9092 |
| `kafka-ui` | provectuslabs/kafka-ui | 8083 |
| `microdesk-booking` | local build | 8081 |
| `microdesk-notification` | local build | 8082 |

---

## Services

- **booking-service** — handles users, desks, and reservations
- **notification-service** — consumes Kafka events and stores notifications

---

## Unit Tests

Tests use JUnit 5 + Mockito. No database or Kafka connection is required — all repositories and external dependencies are mocked.

### booking-service

```
cd booking-service
./gradlew test --tests "com.rasirom.booking_service.service.*"
```

#### `ReservationServiceTest` (11 tests)

| Test | What it verifies |
|------|-----------------|
| `create_happyPath_returnsReservationResponse` | Creates a reservation and returns correct response data |
| `create_deskNotFound_throwsIllegalArgument` | Rejects booking when desk doesn't exist or is inactive |
| `create_userAlreadyHasReservationThatDay_throwsIllegalState` | Prevents a user from double-booking the same day |
| `create_deskAlreadyBookedThatDay_throwsIllegalState` | Prevents two users from booking the same desk on the same day |
| `cancel_happyPath_returnsCancelledResponse` | Cancels a future reservation and returns `CANCELLED` status |
| `cancel_reservationNotFound_throwsIllegalArgument` | Rejects cancellation of a non-existent reservation |
| `cancel_notOwner_throwsSecurityException` | Prevents a user from cancelling another user's reservation |
| `cancel_alreadyCancelled_throwsIllegalState` | Rejects cancellation of an already-cancelled reservation |
| `cancel_pastReservation_throwsIllegalState` | Rejects cancellation of a past reservation |
| `cancel_todayReservation_throwsIllegalState` | Rejects cancellation of a same-day reservation |
| `getMyReservations_returnsListMappedCorrectly` | Maps reservation entities to response DTOs correctly |

#### `UserServiceTest` (6 tests)

| Test | What it verifies |
|------|-----------------|
| `register_newEmail_savesAndReturnsUser` | Registers a new user and encodes the password |
| `register_duplicateEmail_throwsIllegalArgument` | Rejects registration when the email is already in use |
| `login_validCredentials_returnsTokenAndUserInfo` | Returns a JWT token and user info on successful login |
| `login_emailNotFound_throwsIllegalArgument` | Rejects login with an unrecognised email |
| `login_wrongPassword_throwsIllegalArgument` | Rejects login with an incorrect password |
| `login_wrongPassword_neverGeneratesToken` | Confirms no JWT is generated when password is wrong |

### notification-service

```
cd notification-service
./gradlew test --tests "com.rasirom.notification_service.service.*" --tests "com.rasirom.notification_service.consumer.*"
```

#### `NotificationServiceTest` (5 tests)

| Test | What it verifies |
|------|-----------------|
| `process_createdEvent_savesNotificationWithCorrectFields` | Maps all event fields onto the saved `Notification` entity |
| `process_cancelledEvent_savesNotificationWithCancelledType` | Correctly stores a `CANCELLED` event type |
| `process_duplicateEvent_checkedFirst_skipsWithoutSaving` | Skips saving when the repository duplicate-check returns true |
| `process_duplicateDetectedByUniqueConstraint_doesNotPropagateException` | Swallows `DataIntegrityViolationException` from the DB unique constraint |
| `process_createdAndCancelledForSameReservation_savesEachEventOnce` | Both event types for the same reservation are saved independently |

#### `ReservationCreatedConsumerTest` (3 tests)

| Test | What it verifies |
|------|-----------------|
| `consume_successfulProcessing_acknowledgesMessage` | Calls `ack.acknowledge()` after successful processing |
| `consume_serviceThrowsException_doesNotAcknowledge` | Suppresses the exception and does **not** acknowledge, so Kafka retries |
| `consume_nullEventValue_doesNotAcknowledge` | Null payload triggers exception path — message is not acknowledged |
