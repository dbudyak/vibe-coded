package com.leaderboard

import com.leaderboard.database.DatabaseFactory
import com.leaderboard.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseFactory.init()

    // Configure plugins
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureStatusPages()
    configureRouting()
}
