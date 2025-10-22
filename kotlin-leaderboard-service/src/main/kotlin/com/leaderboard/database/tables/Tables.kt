package com.leaderboard.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Games : Table("games") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val apiKey = varchar("api_key", 64).uniqueIndex()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)
}

object Players : Table("players") {
    val id = uuid("id").autoGenerate()
    val gameId = uuid("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val playerId = varchar("player_id", 255)
    val displayName = varchar("display_name", 255).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(gameId, playerId)
    }
}

object Scores : Table("scores") {
    val id = uuid("id").autoGenerate()
    val gameId = uuid("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val playerId = uuid("player_id").references(Players.id, onDelete = ReferenceOption.CASCADE)
    val score = long("score")
    val submittedAt = timestamp("submitted_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)

    init {
        index(true, gameId, score)
        index(false, playerId, score)
        index(false, submittedAt)
    }
}
