package com.leaderboard.models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GameRegistration(
    val name: String,
    val description: String? = null
)

@Serializable
data class GameResponse(
    val id: String,
    val name: String,
    val apiKey: String,
    val description: String? = null,
    val createdAt: String
)

@Serializable
data class ScoreSubmission(
    val playerId: String,
    val displayName: String? = null,
    val score: Long,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class ScoreResponse(
    val id: String,
    val playerId: String,
    val displayName: String?,
    val score: Long,
    val metadata: String,
    val submittedAt: String
)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val playerId: String,
    val displayName: String?,
    val score: Long,
    val submittedAt: String
)

@Serializable
data class LeaderboardResponse(
    val entries: List<LeaderboardEntry>,
    val totalPlayers: Int,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class PlayerStats(
    val playerId: String,
    val displayName: String?,
    val bestScore: Long,
    val totalScores: Int,
    val currentRank: Int?,
    val lastSubmission: String?
)

@Serializable
data class ApiError(
    val error: String,
    val message: String,
    val timestamp: String = java.time.Instant.now().toString()
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String = java.time.Instant.now().toString(),
    val version: String = "1.0.0"
)
