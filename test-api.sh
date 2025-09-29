#!/bin/bash

# Kotlin Leaderboard Service - API Test Suite
# This script contains test requests for all API endpoints

echo "🎮 Kotlin Leaderboard Service - API Tests"
echo "=========================================="
echo ""

# Base URL
BASE_URL="http://localhost:8080/api/v1"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Test 1: Health Check
echo -e "${BLUE}Test 1: Health Check${NC}"
echo "GET $BASE_URL/health"
curl -s $BASE_URL/health | jq '.'
echo ""
echo ""

# Test 2: Register a Game
echo -e "${BLUE}Test 2: Register a Game${NC}"
echo "POST $BASE_URL/games/register"
GAME_RESPONSE=$(curl -s -X POST $BASE_URL/games/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Game",
    "description": "A test game for API testing"
  }')

echo "$GAME_RESPONSE" | jq '.'

# Extract API key from response
API_KEY=$(echo $GAME_RESPONSE | jq -r '.apiKey')
echo ""
echo -e "${GREEN}API Key: $API_KEY${NC}"
echo -e "${GREEN}Save this API key for subsequent requests!${NC}"
echo ""
echo ""

# Test 3: Submit Score #1
echo -e "${BLUE}Test 3: Submit Score for Player 1${NC}"
echo "POST $BASE_URL/scores"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "playerId": "player1",
    "displayName": "ProGamer",
    "score": 9999,
    "metadata": {
      "level": "10",
      "difficulty": "hard"
    }
  }' | jq '.'
echo ""
echo ""

# Test 4: Submit Score #2
echo -e "${BLUE}Test 4: Submit Score for Player 2${NC}"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "playerId": "player2",
    "displayName": "ElitePlayer",
    "score": 8888,
    "metadata": {
      "level": "9",
      "difficulty": "medium"
    }
  }' | jq '.'
echo ""
echo ""

# Test 5: Submit Score #3
echo -e "${BLUE}Test 5: Submit Score for Player 3${NC}"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "playerId": "player3",
    "displayName": "Rookie",
    "score": 5000
  }' | jq '.'
echo ""
echo ""

# Test 6: Submit another score for Player 1 (higher score)
echo -e "${BLUE}Test 6: Submit Higher Score for Player 1${NC}"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "playerId": "player1",
    "displayName": "ProGamer",
    "score": 12000
  }' | jq '.'
echo ""
echo ""

# Test 7: Get Global Leaderboard
echo -e "${BLUE}Test 7: Get Global Leaderboard (Top 10)${NC}"
echo "GET $BASE_URL/leaderboard/global?page=1&pageSize=10"
curl -s "$BASE_URL/leaderboard/global?page=1&pageSize=10" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 8: Get Global Leaderboard (Paginated)
echo -e "${BLUE}Test 8: Get Global Leaderboard (Page 2)${NC}"
echo "GET $BASE_URL/leaderboard/global?page=2&pageSize=5"
curl -s "$BASE_URL/leaderboard/global?page=2&pageSize=5" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 9: Get Daily Leaderboard
echo -e "${BLUE}Test 9: Get Daily Leaderboard${NC}"
echo "GET $BASE_URL/leaderboard/daily"
curl -s "$BASE_URL/leaderboard/daily?page=1&pageSize=10" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 10: Get Weekly Leaderboard
echo -e "${BLUE}Test 10: Get Weekly Leaderboard${NC}"
echo "GET $BASE_URL/leaderboard/weekly"
curl -s "$BASE_URL/leaderboard/weekly?page=1&pageSize=10" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 11: Get Monthly Leaderboard
echo -e "${BLUE}Test 11: Get Monthly Leaderboard${NC}"
echo "GET $BASE_URL/leaderboard/monthly"
curl -s "$BASE_URL/leaderboard/monthly?page=1&pageSize=10" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 12: Get Player Stats
echo -e "${BLUE}Test 12: Get Player Stats for player1${NC}"
echo "GET $BASE_URL/players/player1"
curl -s "$BASE_URL/players/player1" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 13: Get Player Rank
echo -e "${BLUE}Test 13: Get Player Rank for player1${NC}"
echo "GET $BASE_URL/players/player1/rank"
curl -s "$BASE_URL/players/player1/rank" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 14: Get Player Stats for player2
echo -e "${BLUE}Test 14: Get Player Stats for player2${NC}"
curl -s "$BASE_URL/players/player2" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 15: Get Player Rank for player3
echo -e "${BLUE}Test 15: Get Player Rank for player3${NC}"
curl -s "$BASE_URL/players/player3/rank" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Error Tests
echo -e "${RED}=== Error Tests ===${NC}"
echo ""

# Test 16: Invalid API Key
echo -e "${BLUE}Test 16: Invalid API Key${NC}"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: invalid_key" \
  -d '{
    "playerId": "player99",
    "displayName": "Test",
    "score": 100
  }' | jq '.'
echo ""
echo ""

# Test 17: Missing API Key
echo -e "${BLUE}Test 17: Missing API Key${NC}"
curl -s "$BASE_URL/leaderboard/global" | jq '.'
echo ""
echo ""

# Test 18: Invalid Score (negative)
echo -e "${BLUE}Test 18: Invalid Score (negative)${NC}"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "playerId": "player99",
    "displayName": "Test",
    "score": -100
  }' | jq '.'
echo ""
echo ""

# Test 19: Empty Player ID
echo -e "${BLUE}Test 19: Empty Player ID${NC}"
curl -s -X POST $BASE_URL/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $API_KEY" \
  -d '{
    "playerId": "",
    "displayName": "Test",
    "score": 100
  }' | jq '.'
echo ""
echo ""

# Test 20: Non-existent Player
echo -e "${BLUE}Test 20: Get Stats for Non-existent Player${NC}"
curl -s "$BASE_URL/players/nonexistent_player" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 21: Invalid Page Number
echo -e "${BLUE}Test 21: Invalid Page Number${NC}"
curl -s "$BASE_URL/leaderboard/global?page=0&pageSize=10" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

# Test 22: Invalid Page Size
echo -e "${BLUE}Test 22: Invalid Page Size (too large)${NC}"
curl -s "$BASE_URL/leaderboard/global?page=1&pageSize=1000" \
  -H "X-API-Key: $API_KEY" | jq '.'
echo ""
echo ""

echo -e "${GREEN}=========================================="
echo -e "All tests completed!"
echo -e "==========================================${NC}"
