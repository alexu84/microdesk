#!/bin/bash

TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpYXQiOjE3ODEwMDYxMDksImV4cCI6MTc4MTA5MjUwOX0.FixJFws2cSLi5PDzRJUES9VmnqM3pUQIFOKM0ZhUBh8"
RESERVATION_ID=1

curl -X PATCH "http://localhost:8080/reservations/$RESERVATION_ID/cancel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"reason":"I will be working from home that day"}'
