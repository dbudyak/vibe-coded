# 🎮 Kotlin Leaderboard Service

A production-ready, open-source leaderboard service built with Kotlin, Ktor, and PostgreSQL. Perfect for game developers who need a reliable, scalable solution for storing player scores and managing leaderboards.

## ✨ Features

- 🚀 **Easy Deployment** - One-command Docker deployment
- 🔐 **Secure Authentication** - API key-based authentication per game
- 📊 **Multiple Leaderboard Types** - Global, daily, weekly, and monthly leaderboards
- 🎯 **Player Statistics** - Track individual player stats and rankings
- 💾 **PostgreSQL Backend** - Reliable, scalable data storage
- 🔄 **RESTful API** - Clean, well-documented API
- ⚡ **High Performance** - Optimized database queries and indexing
- 🐳 **Docker Ready** - Includes Docker and docker-compose configuration

## 🚀 Quick Start

### Prerequisites
- Docker 20.10+
- Docker Compose 2.0+

### Installation

1. **Set up environment**
```bash
cp .env.example .env
nano .env  # Set a secure DB_PASSWORD
```

2. **Start the service**
```bash
docker-compose up -d
```

3. **Verify it's running**
```bash
curl http://localhost:8080/api/v1/health
```

## 📖 Usage

### Register Your Game
```bash
curl -X POST http://localhost:8080/api/v1/games/register \
  -H "Content-Type: application/json" \
  -d '{"name": "My Game", "description": "An awesome game"}'
```

Save the returned `apiKey`!

### Submit a Score
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your_api_key_here" \
  -d '{"playerId": "player123", "displayName": "ProGamer", "score": 9999}'
```

### Get Leaderboard
```bash
curl http://localhost:8080/api/v1/leaderboard/global?page=1&pageSize=10 \
  -H "X-API-Key: your_api_key_here"
```

## 🛠️ Technology Stack

- **Framework:** Ktor 2.3.7
- **Language:** Kotlin 1.9.22
- **Database:** PostgreSQL 16
- **ORM:** Exposed
- **Cache:** Redis 7
- **Containerization:** Docker & Docker Compose

## 📁 Project Structure

```
kotlin-leaderboard-service/
├── src/main/kotlin/com/leaderboard/
│   ├── Application.kt
│   ├── database/
│   │   ├── DatabaseFactory.kt
│   │   └── tables/Tables.kt
│   ├── models/Models.kt
│   ├── services/
│   │   ├── GameService.kt
│   │   ├── ScoreService.kt
│   │   └── LeaderboardService.kt
│   ├── routes/
│   │   ├── GameRoutes.kt
│   │   ├── ScoreRoutes.kt
│   │   └── LeaderboardRoutes.kt
│   └── plugins/
│       ├── Routing.kt
│       ├── Security.kt
│       └── ...
├── docker-compose.yml
├── Dockerfile
└── README.md
```

## 🔧 Development

### Local Development (without Docker)

1. Install PostgreSQL and create database:
```bash
createdb leaderboard
```

2. Set environment variables:
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/leaderboard
export DATABASE_USER=your_username
export DATABASE_PASSWORD=your_password
```

3. Run the application:
```bash
./gradlew run
```

### Build and Test
```bash
./gradlew build
./gradlew test
./gradlew buildFatJar
```

## 🚢 Deployment

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## 📊 API Endpoints

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/health` | GET | No | Health check |
| `/games/register` | POST | No | Register a new game |
| `/scores` | POST | Yes | Submit a score |
| `/leaderboard/global` | GET | Yes | Global leaderboard |
| `/leaderboard/daily` | GET | Yes | Last 24 hours |
| `/leaderboard/weekly` | GET | Yes | Last 7 days |
| `/leaderboard/monthly` | GET | Yes | Last 30 days |
| `/players/{id}` | GET | Yes | Player statistics |
| `/players/{id}/rank` | GET | Yes | Player's rank |

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

Built with [Ktor](https://ktor.io) framework and [Exposed](https://github.com/JetBrains/Exposed) ORM.

---

**Made with ❤️ for game developers**
