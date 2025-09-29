package com.github.leaderboard.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import com.github.leaderboard.database.tables.*

object DatabaseFactory {
    
    fun init(config: ApplicationConfig) {
        val databaseConfig = config.config("database")
        val database = Database.connect(createHikariDataSource(databaseConfig))
        
        transaction(database) {
            SchemaUtils.create(Games, Players, Scores)
        }
    }
    
    private fun createHikariDataSource(config: ApplicationConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            driverClassName = config.property("driver").getString()
            jdbcUrl = config.property("url").getString()
            username = config.property("user").getString()
            password = config.property("password").getString()
            maximumPoolSize = config.property("maxPoolSize").getString().toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hikariConfig)
    }
    
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
