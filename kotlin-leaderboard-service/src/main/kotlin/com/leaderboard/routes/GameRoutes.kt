package com.leaderboard.routes

import com.leaderboard.models.GameRegistration
import com.leaderboard.services.GameService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.gameRoutes() {
    val gameService = GameService()

    route("/games") {
        post("/register") {
            val registration = call.receive<GameRegistration>()

            if (registration.name.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Game name cannot be empty")
                )
                return@post
            }

            val game = gameService.registerGame(registration)
            call.respond(HttpStatusCode.Created, game)
        }
    }
}
