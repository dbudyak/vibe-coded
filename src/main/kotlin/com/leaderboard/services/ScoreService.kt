package com.leaderboard.services

import com.leaderboard.database.DatabaseFactory.dbQuery
import com.leaderboard.database.tables.Players
import com.leaderboard.database.tables.Scores
import com.leaderboard.models.ScoreResponse
import com.leaderboard.models.ScoreSubmission
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import java.util.*

class ScoreService {

    suspend fun submitScore(gameId: UUID, submission: ScoreSubmission): ScoreResponse = dbQuery {
        // Get or create player
        val playerId = getOrCreatePlayer(gameId, submission.playerId, submission.displayName)

        // Insert score
        val insertStatement = Scores.insert {
            it[Scores.gameId] = gameId
            it[Scores.playerId] = playerId
            it[score] = submission.score
            it[metadata] = Json.encodeToString(submission.metadata)
        }

        val result = insertStatement.resultedValues!!.first()

        ScoreResponse(
            id = result[Scores.id].toString(),
            playerId = submission.playerId,
            displayName = submission.displayName,
            score = result[Scores.score],
            metadata = result[Scores.metadata],
            submittedAt = result[Scores.submittedAt].toString()
        )
    }

    suspend fun getPlayerBestScore(gameId: UUID, playerId: String): Long? = dbQuery {
        val playerUuid = Players
            .select { (Players.gameId eq gameId) and (Players.playerId eq playerId) }
            .map { it[Players.id] }
            .singleOrNull()

        playerUuid?.let {
            Scores
                .select { (Scores.gameId eq gameId) and (Scores.playerId eq it) }
                .maxOfOrNull { row -> row[Scores.score] }
        }
    }

    suspend fun getPlayerScoreCount(gameId: UUID, playerId: UUID): Int = dbQuery {
        Scores
            .select { (Scores.gameId eq gameId) and (Scores.playerId eq playerId) }
            .count()
            .toInt()
    }

    private suspend fun getOrCreatePlayer(gameId: UUID, playerId: String, displayName: String?): UUID {
        val existing = Players
            .select { (Players.gameId eq gameId) and (Players.playerId eq playerId) }
            .map { it[Players.id] }
            .singleOrNull()

        if (existing != null) {
            // Update display name if provided
            if (displayName != null) {
                Players.update({ (Players.gameId eq gameId) and (Players.playerId eq playerId) }) {
                    it[Players.displayName] = displayName
                }
            }
            return existing
        }

        // Create new player
        val insertStatement = Players.insert {
            it[Players.gameId] = gameId
            it[Players.playerId] = playerId
            it[Players.displayName] = displayName
        }

        return insertStatement.resultedValues!!.first()[Players.id]
    }
}
