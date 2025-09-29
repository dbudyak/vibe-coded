package com.leaderboard.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.callloging.processingTimeMillis
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val duration = call.processingTimeMillis()
            "$httpMethod $path - $status (${duration}ms)"
        }
    }
}
