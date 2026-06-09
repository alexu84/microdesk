#!/bin/bash

TOKEN="<your_jwt_token>"

curl -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"deskId":1,"day":"2026-06-10"}'
