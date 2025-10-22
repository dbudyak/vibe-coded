#!/bin/bash

# Test script for Sky Report API

BASE_URL="http://localhost:8080"

echo "Testing Sky Report API"
echo "====================="
echo ""

echo "1. Health Check:"
curl -s "$BASE_URL/health" | jq .
echo -e "\n"

echo "2. New York City (urban, high light pollution):"
curl -s "$BASE_URL/api/v1/sky-condition?lat=40.7128&lon=-74.0060" | jq .
echo -e "\n"

echo "3. Cherry Springs State Park, PA (dark sky site):"
curl -s "$BASE_URL/api/v1/sky-condition?lat=41.6628&lon=-77.8209" | jq .
echo -e "\n"

echo "4. Death Valley, CA (excellent dark sky):"
curl -s "$BASE_URL/api/v1/sky-condition?lat=36.5054&lon=-117.0794" | jq .
echo -e "\n"

echo "5. London, UK:"
curl -s "$BASE_URL/api/v1/sky-condition?lat=51.5074&lon=-0.1278" | jq .
echo -e "\n"

echo "6. POST request with specific time:"
curl -s -X POST "$BASE_URL/api/v1/sky-condition" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 19.8207,
    "longitude": -155.4681,
    "time": "2025-10-22T22:00:00Z"
  }' | jq .
echo -e "\n"

echo "7. Test error handling (invalid coordinates):"
curl -s "$BASE_URL/api/v1/sky-condition?lat=999&lon=-74.0060" | jq .
echo -e "\n"
