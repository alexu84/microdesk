#!/bin/bash

TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpYXQiOjE3ODEwMDYxMDksImV4cCI6MTc4MTA5MjUwOX0.FixJFws2cSLi5PDzRJUES9VmnqM3pUQIFOKM0ZhUBh8"

curl -X GET http://localhost:8080/reservations \
  -H "Authorization: Bearer $TOKEN"
