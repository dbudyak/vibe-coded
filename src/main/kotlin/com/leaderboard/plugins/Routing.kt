package com.leaderboard.plugins

import com.leaderboard.models.HealthResponse
import com.leaderboard.routes.gameRoutes
import com.leaderboard.routes.leaderboardRoutes
import com.leaderboard.routes.scoreRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            // Health check endpoint (no authentication required)
            get("/health") {
                call.respond(HealthResponse(status = "UP"))
            }

            // Register routes
            gameRoutes()
            scoreRoutes()
            leaderboardRoutes()
        }
    }
}
