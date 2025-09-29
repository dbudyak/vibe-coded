package com.github.leaderboard.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Games : Table("games") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val apiKey = varchar("api_key", 64).uniqueIndex()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    
    override val primaryKey = PrimaryKey(id)
}
