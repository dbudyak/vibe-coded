package com.leaderboard.services

import com.leaderboard.database.DatabaseFactory.dbQuery
import com.leaderboard.database.tables.Players
import com.leaderboard.database.tables.Scores
import com.leaderboard.models.LeaderboardEntry
import com.leaderboard.models.LeaderboardResponse
import com.leaderboard.models.PlayerStats
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class LeaderboardService {

    suspend fun getGlobalLeaderboard(
        gameId: UUID,
        page: Int = 1,
        pageSize: Int = 100
    ): LeaderboardResponse = dbQuery {
        val offset = ((page - 1) * pageSize).toLong()

        // Get best score per player
        val bestScores = Scores
            .slice(Scores.playerId, Scores.score.max(), Scores.submittedAt.max())
            .select { Scores.gameId eq gameId }
            .groupBy(Scores.playerId)
            .orderBy(Scores.score.max(), SortOrder.DESC)
            .limit(pageSize, offset)
            .toList()

        val entries = bestScores.mapIndexed { index, row ->
            val playerId = row[Scores.playerId]
            val playerInfo = Players.select { Players.id eq playerId }.single()

            LeaderboardEntry(
                rank = offset.toInt() + index + 1,
                playerId = playerInfo[Players.playerId],
                displayName = playerInfo[Players.displayName],
                score = row[Scores.score.max()]!!,
                submittedAt = row[Scores.submittedAt.max()].toString()
            )
        }

        val totalPlayers = getTotalPlayers(gameId)

        LeaderboardResponse(
            entries = entries,
            totalPlayers = totalPlayers,
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun getTimeBasedLeaderboard(
        gameId: UUID,
        timeUnit: ChronoUnit,
        page: Int = 1,
        pageSize: Int = 100
    ): LeaderboardResponse = dbQuery {
        val cutoffTime = when (timeUnit) {
            ChronoUnit.DAYS -> Instant.now().minus(1, ChronoUnit.DAYS)
            ChronoUnit.WEEKS -> Instant.now().minus(7, ChronoUnit.DAYS)
            ChronoUnit.MONTHS -> Instant.now().minus(30, ChronoUnit.DAYS)
            else -> Instant.now().minus(1, ChronoUnit.DAYS)
        }

        val offset = ((page - 1) * pageSize).toLong()

        val bestScores = Scores
            .slice(Scores.playerId, Scores.score.max(), Scores.submittedAt.max())
            .select { (Scores.gameId eq gameId) and (Scores.submittedAt greaterEq cutoffTime) }
            .groupBy(Scores.playerId)
            .orderBy(Scores.score.max(), SortOrder.DESC)
            .limit(pageSize, offset)
            .toList()

        val entries = bestScores.mapIndexed { index, row ->
            val playerId = row[Scores.playerId]
            val playerInfo = Players.select { Players.id eq playerId }.single()

            LeaderboardEntry(
                rank = offset.toInt() + index + 1,
                playerId = playerInfo[Players.playerId],
                displayName = playerInfo[Players.displayName],
                score = row[Scores.score.max()]!!,
                submittedAt = row[Scores.submittedAt.max()].toString()
            )
        }

        val totalPlayers = getTotalPlayersInTimeRange(gameId, cutoffTime)

        LeaderboardResponse(
            entries = entries,
            totalPlayers = totalPlayers,
            page = page,
            pageSize = pageSize
        )
    }

    suspend fun getPlayerStats(gameId: UUID, playerId: String): PlayerStats? = dbQuery {
        val playerUuid = Players
            .select { (Players.gameId eq gameId) and (Players.playerId eq playerId) }
            .map { it[Players.id] to it[Players.displayName] }
            .singleOrNull() ?: return@dbQuery null

        val scores = Scores
            .select { (Scores.gameId eq gameId) and (Scores.playerId eq playerUuid.first) }
            .toList()

        if (scores.isEmpty()) return@dbQuery null

        val bestScore = scores.maxOf { it[Scores.score] }
        val totalScores = scores.size
        val lastSubmission = scores.maxOf { it[Scores.submittedAt] }

        val rank = calculatePlayerRank(gameId, bestScore)

        PlayerStats(
            playerId = playerId,
            displayName = playerUuid.second,
            bestScore = bestScore,
            totalScores = totalScores,
            currentRank = rank,
            lastSubmission = lastSubmission.toString()
        )
    }

    suspend fun getPlayerRank(gameId: UUID, playerId: String): Int? = dbQuery {
        val playerUuid = Players
            .select { (Players.gameId eq gameId) and (Players.playerId eq playerId) }
            .map { it[Players.id] }
            .singleOrNull() ?: return@dbQuery null

        val bestScore = Scores
            .select { (Scores.gameId eq gameId) and (Scores.playerId eq playerUuid) }
            .maxOfOrNull { it[Scores.score] } ?: return@dbQuery null

        calculatePlayerRank(gameId, bestScore)
    }

    private fun calculatePlayerRank(gameId: UUID, score: Long): Int {
        // Count how many players have a better best score
        val betterScores = Scores
            .slice(Scores.playerId, Scores.score.max())
            .select { Scores.gameId eq gameId }
            .groupBy(Scores.playerId)
            .having { Scores.score.max() greater score }
            .count()

        return betterScores.toInt() + 1
    }

    private fun getTotalPlayers(gameId: UUID): Int {
        return Scores
            .slice(Scores.playerId)
            .select { Scores.gameId eq gameId }
            .withDistinct()
            .count()
            .toInt()
    }

    private fun getTotalPlayersInTimeRange(gameId: UUID, cutoffTime: Instant): Int {
        return Scores
            .slice(Scores.playerId)
            .select { (Scores.gameId eq gameId) and (Scores.submittedAt greaterEq cutoffTime) }
            .withDistinct()
            .count()
            .toInt()
    }
}
