package br.com.rinha.backend

import br.com.rinha.backend.plugins.configureRouting
import br.com.rinha.backend.plugins.configureSerialization
import io.ebean.DB
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    DB.sqlQuery("select 1").findOne() // Load ORM
    embeddedServer(
        Netty,
        port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 8080,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
