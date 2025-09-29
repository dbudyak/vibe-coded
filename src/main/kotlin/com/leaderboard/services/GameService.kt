package com.leaderboard.services

import com.leaderboard.database.DatabaseFactory.dbQuery
import com.leaderboard.database.tables.Games
import com.leaderboard.models.GameRegistration
import com.leaderboard.models.GameResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.security.SecureRandom
import java.util.*

class GameService {
    
    suspend fun registerGame(registration: GameRegistration): GameResponse = dbQuery {
        val apiKey = generateApiKey()
        
        val insertStatement = Games.insert {
            it[name] = registration.name
            it[Games.apiKey] = apiKey
            it[description] = registration.description
        }

        val result = insertStatement.resultedValues!!.first()
        
        GameResponse(
            id = result[Games.id].toString(),
            name = result[Games.name],
            apiKey = result[Games.apiKey],
            description = result[Games.description],
            createdAt = result[Games.createdAt].toString()
        )
    }

    suspend fun validateApiKey(apiKey: String): UUID? = dbQuery {
        Games.select { Games.apiKey eq apiKey }
            .map { it[Games.id] }
            .singleOrNull()
    }

    private fun generateApiKey(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
