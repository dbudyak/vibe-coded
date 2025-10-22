# API Test Commands for Kotlin Leaderboard Service

## Setup
Base URL: `http://localhost:8080/api/v1`

---

## 1. Health Check (No Auth Required)

```bash
curl http://localhost:8080/api/v1/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "timestamp": "2025-09-29T...",
  "version": "1.0.0"
}
```

---

## 2. Register a Game (No Auth Required)

```bash
curl -X POST http://localhost:8080/api/v1/games/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Awesome Game",
    "description": "An epic adventure game"
  }'
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Awesome Game",
  "apiKey": "a1b2c3d4e5f6g7h8...",
  "description": "An epic adventure game",
  "createdAt": "2025-09-29T..."
}
```

**⚠️ SAVE THE API KEY!** You'll need it for all subsequent requests.

---

## 3. Submit Scores

### Submit Score #1
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -d '{
    "playerId": "player1",
    "displayName": "ProGamer",
    "score": 9999,
    "metadata": {
      "level": "10",
      "difficulty": "hard",
      "playtime": "3600"
    }
  }'
```

### Submit Score #2
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -d '{
    "playerId": "player2",
    "displayName": "ElitePlayer",
    "score": 8888
  }'
```

### Submit Score #3
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -d '{
    "playerId": "player3",
    "displayName": "Rookie",
    "score": 5000
  }'
```

### Submit Higher Score (Update)
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -d '{
    "playerId": "player1",
    "displayName": "ProGamer",
    "score": 15000
  }'
```

---

## 4. Get Global Leaderboard

### Get Top 10
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=1&pageSize=10 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

### Get Top 50
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=1&pageSize=50 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

### Get Page 2 (11-20)
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=2&pageSize=10 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

**Expected Response:**
```json
{
  "entries": [
    {
      "rank": 1,
      "playerId": "player1",
      "displayName": "ProGamer",
      "score": 15000,
      "submittedAt": "2025-09-29T..."
    },
    {
      "rank": 2,
      "playerId": "player2",
      "displayName": "ElitePlayer",
      "score": 8888,
      "submittedAt": "2025-09-29T..."
    }
  ],
  "totalPlayers": 3,
  "page": 1,
  "pageSize": 10
}
```

---

## 5. Get Time-Based Leaderboards

### Daily Leaderboard (Last 24 hours)
```bash
curl http://localhost:8080/api/v1/leaderboard/daily?page=1&pageSize=10 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

### Weekly Leaderboard (Last 7 days)
```bash
curl http://localhost:8080/api/v1/leaderboard/weekly?page=1&pageSize=10 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

### Monthly Leaderboard (Last 30 days)
```bash
curl http://localhost:8080/api/v1/leaderboard/monthly?page=1&pageSize=10 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

---

## 6. Get Player Statistics

### Get Player Stats
```bash
curl http://localhost:8080/api/v1/players/player1 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

**Expected Response:**
```json
{
  "playerId": "player1",
  "displayName": "ProGamer",
  "bestScore": 15000,
  "totalScores": 2,
  "currentRank": 1,
  "lastSubmission": "2025-09-29T..."
}
```

### Get Another Player's Stats
```bash
curl http://localhost:8080/api/v1/players/player2 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

---

## 7. Get Player Rank

### Get Player Rank
```bash
curl http://localhost:8080/api/v1/players/player1/rank \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

**Expected Response:**
```json
{
  "playerId": "player1",
  "rank": 1
}
```

### Get Another Player's Rank
```bash
curl http://localhost:8080/api/v1/players/player3/rank \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

---

## Error Test Cases

### Invalid API Key
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: invalid_api_key" \
  -d '{
    "playerId": "player99",
    "displayName": "Test",
    "score": 100
  }'
```

**Expected Response:**
```json
{
  "error": "Invalid or missing API key"
}
```

### Missing API Key
```bash
curl http://localhost:8080/api/v1/leaderboard/global
```

**Expected Response:**
```json
{
  "error": "Invalid or missing API key"
}
```

### Negative Score
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -d '{
    "playerId": "player99",
    "displayName": "Test",
    "score": -100
  }'
```

**Expected Response:**
```json
{
  "error": "Score cannot be negative"
}
```

### Empty Player ID
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY_HERE" \
  -d '{
    "playerId": "",
    "displayName": "Test",
    "score": 100
  }'
```

**Expected Response:**
```json
{
  "error": "Player ID cannot be empty"
}
```

### Non-existent Player
```bash
curl http://localhost:8080/api/v1/players/nonexistent_player \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

**Expected Response:**
```json
{
  "error": "NotFound",
  "message": "Player not found"
}
```

### Invalid Page Number
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=0&pageSize=10 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

**Expected Response:**
```json
{
  "error": "Page must be >= 1"
}
```

### Invalid Page Size
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=1&pageSize=1000 \
  -H "X-API-Key: YOUR_API_KEY_HERE"
```

**Expected Response:**
```json
{
  "error": "Page size must be between 1 and 500"
}
```

---

## Bulk Testing Commands

### Create Multiple Players Quickly
```bash
# Create 10 players with random scores
for i in {1..10}; do
  SCORE=$((RANDOM % 10000 + 1000))
  curl -X POST http://localhost:8080/api/v1/scores \
    -H "Content-Type: application/json" \
    -H "X-API-Key: YOUR_API_KEY_HERE" \
    -d "{
      \"playerId\": \"player$i\",
      \"displayName\": \"Player $i\",
      \"score\": $SCORE
    }"
  echo ""
done
```

### Then Check the Leaderboard
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=1&pageSize=20 \
  -H "X-API-Key: YOUR_API_KEY_HERE" | jq '.'
```

---

## Pretty Print with jq

Add `| jq '.'` to any command for pretty-printed JSON:

```bash
curl http://localhost:8080/api/v1/health | jq '.'
```

---

## Quick Test Workflow

1. **Start the service:**
   ```bash
   docker-compose up -d
   ```

2. **Check health:**
   ```bash
   curl http://localhost:8080/api/v1/health
   ```

3. **Register game and save API key:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/games/register \
     -H "Content-Type: application/json" \
     -d '{"name": "Test Game", "description": "Testing"}' | jq '.'
   ```

4. **Export API key:**
   ```bash
   export API_KEY="your_api_key_here"
   ```

5. **Submit test scores:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/scores \
     -H "Content-Type: application/json" \
     -H "X-API-Key: $API_KEY" \
     -d '{"playerId": "test1", "displayName": "Tester", "score": 1000}'
   ```

6. **View leaderboard:**
   ```bash
   curl http://localhost:8080/api/v1/leaderboard/global \
     -H "X-API-Key: $API_KEY" | jq '.'
   ```
