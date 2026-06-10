#!/bin/bash

TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpYXQiOjE3ODEwODA1NzMsImV4cCI6MTc4MTE2Njk3M30.noht_ktsjawZV7sqloJW6HQ5g1O6h_wUweq8T5sFK2E"

curl -X GET http://localhost:8080/notifications \
  -H "Authorization: Bearer $TOKEN"
