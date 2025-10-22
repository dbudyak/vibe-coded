package com.leaderboard.routes

import com.leaderboard.models.ScoreSubmission
import com.leaderboard.plugins.authenticated
import com.leaderboard.plugins.getGameId
import com.leaderboard.services.ScoreService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.scoreRoutes() {
    val scoreService = ScoreService()

    authenticated {
        route("/scores") {
            post {
                val gameId = call.getGameId()
                val submission = call.receive<ScoreSubmission>()

                // Validation
                if (submission.playerId.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Player ID cannot be empty")
                    )
                    return@post
                }

                if (submission.score < 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Score cannot be negative")
                    )
                    return@post
                }

                val score = scoreService.submitScore(gameId, submission)
                call.respond(HttpStatusCode.Created, score)
            }
        }
    }
}
