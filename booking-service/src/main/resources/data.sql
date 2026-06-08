INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 1, 'A101', 1, 'Near window', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 1 AND room_number = 'A101');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 2, 'A101', 1, 'Near door', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 2 AND room_number = 'A101');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 3, 'A101', 1, 'Standing desk', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 3 AND room_number = 'A101');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 4, 'A101', 1, 'Corner desk', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 4 AND room_number = 'A101');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 5, 'A101', 1, 'Near printer', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 5 AND room_number = 'A101');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 1, 'B201', 2, 'Near window', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 1 AND room_number = 'B201');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 2, 'B201', 2, 'Standing desk', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 2 AND room_number = 'B201');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 3, 'B201', 2, 'Quiet corner', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 3 AND room_number = 'B201');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 4, 'B201', 2, 'Near whiteboard', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 4 AND room_number = 'B201');

INSERT INTO desks (desk_number, room_number, floor, description, active, created_at, updated_at)
SELECT 5, 'B201', 2, 'Near door', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM desks WHERE desk_number = 5 AND room_number = 'B201');
