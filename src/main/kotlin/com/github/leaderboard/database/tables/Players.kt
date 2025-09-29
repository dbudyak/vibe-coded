package com.github.leaderboard.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Players : Table("players") {
    val id = uuid("id").autoGenerate()
    val gameId = uuid("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val playerId = varchar("player_id", 255)
    val displayName = varchar("display_name", 255).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        uniqueIndex(gameId, playerId)
    }
}
