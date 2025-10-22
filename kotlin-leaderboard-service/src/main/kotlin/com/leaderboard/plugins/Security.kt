package com.leaderboard.plugins

import com.leaderboard.services.GameService
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import java.util.*

val gameService = GameService()

class ApiKeyPrincipal(val gameId: UUID)

class ApiKeyAuthenticationPlugin(configuration: Configuration) {
    private val headerName = configuration.headerName

    class Configuration {
        var headerName: String = "X-API-Key"
    }

    companion object Plugin : BaseApplicationPlugin<Application, Configuration, ApiKeyAuthenticationPlugin> {
        override val key = AttributeKey<ApiKeyAuthenticationPlugin>("ApiKeyAuthentication")

        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit
        ): ApiKeyAuthenticationPlugin {
            val configuration = Configuration().apply(configure)
            return ApiKeyAuthenticationPlugin(configuration)
        }
    }

    suspend fun authenticate(call: ApplicationCall): ApiKeyPrincipal? {
        val apiKey = call.request.headers[headerName] ?: return null
        val gameId = gameService.validateApiKey(apiKey) ?: return null
        return ApiKeyPrincipal(gameId)
    }
}

fun Application.configureSecurity() {
    install(ApiKeyAuthenticationPlugin) {
        headerName = "X-API-Key"
    }
}

fun Route.authenticated(build: Route.() -> Unit): Route {
    val authenticatedRoute = createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
    })

    authenticatedRoute.intercept(ApplicationCallPipeline.Call) {
        val plugin = application.plugin(ApiKeyAuthenticationPlugin)
        val principal = plugin.authenticate(call)

        if (principal == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing API key"))
            finish()
            return@intercept
        }

        call.attributes.put(AttributeKey("gameId"), principal.gameId)
    }

    authenticatedRoute.build()
    return authenticatedRoute
}

fun ApplicationCall.getGameId(): UUID {
    return attributes[AttributeKey("gameId")]
}
