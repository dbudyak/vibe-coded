package com.github.leaderboard

import com.github.leaderboard.database.DatabaseFactory
import com.github.leaderboard.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseFactory.init(environment.config)
    
    // Configure plugins
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}
