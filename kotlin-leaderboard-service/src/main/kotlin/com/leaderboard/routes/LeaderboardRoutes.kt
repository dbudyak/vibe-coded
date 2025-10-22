package com.leaderboard.routes

import com.leaderboard.plugins.authenticated
import com.leaderboard.plugins.getGameId
import com.leaderboard.services.LeaderboardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.temporal.ChronoUnit

fun Route.leaderboardRoutes() {
    val leaderboardService = LeaderboardService()

    authenticated {
        route("/leaderboard") {
            get("/global") {
                val gameId = call.getGameId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 100

                if (page < 1) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Page must be >= 1"))
                    return@get
                }

                if (pageSize !in 1..500) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Page size must be between 1 and 500"))
                    return@get
                }

                val leaderboard = leaderboardService.getGlobalLeaderboard(gameId, page, pageSize)
                call.respond(leaderboard)
            }

            get("/daily") {
                val gameId = call.getGameId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 100

                val leaderboard = leaderboardService.getTimeBasedLeaderboard(
                    gameId, ChronoUnit.DAYS, page, pageSize
                )
                call.respond(leaderboard)
            }

            get("/weekly") {
                val gameId = call.getGameId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 100

                val leaderboard = leaderboardService.getTimeBasedLeaderboard(
                    gameId, ChronoUnit.WEEKS, page, pageSize
                )
                call.respond(leaderboard)
            }

            get("/monthly") {
                val gameId = call.getGameId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 100

                val leaderboard = leaderboardService.getTimeBasedLeaderboard(
                    gameId, ChronoUnit.MONTHS, page, pageSize
                )
                call.respond(leaderboard)
            }
        }

        route("/players") {
            get("/{playerId}") {
                val gameId = call.getGameId()
                val playerId = call.parameters["playerId"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Player ID required"))
                    return@get
                }

                val stats = leaderboardService.getPlayerStats(gameId, playerId)
                if (stats == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Player not found"))
                    return@get
                }

                call.respond(stats)
            }

            get("/{playerId}/rank") {
                val gameId = call.getGameId()
                val playerId = call.parameters["playerId"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Player ID required"))
                    return@get
                }

                val rank = leaderboardService.getPlayerRank(gameId, playerId)
                if (rank == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Player not found"))
                    return@get
                }

                call.respond(mapOf("playerId" to playerId, "rank" to rank))
            }
        }
    }
}
