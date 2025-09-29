package com.github.leaderboard.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Scores : Table("scores") {
    val id = uuid("id").autoGenerate()
    val gameId = uuid("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val playerId = uuid("player_id").references(Players.id, onDelete = ReferenceOption.CASCADE)
    val score = long("score")
    val metadata = text("metadata").nullable()
    val submittedAt = timestamp("submitted_at").clientDefault { Instant.now() }
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        index(false, gameId, score)
        index(false, playerId, score)
        index(false, submittedAt)
    }
}
