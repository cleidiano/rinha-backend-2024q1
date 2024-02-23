package br.com.rinha.backend.plugins

import br.com.rinha.backend.bank.Accounts
import br.com.rinha.backend.bank.TransactionResult
import br.com.rinha.backend.bank.TransactionResult.ClientNotFound
import br.com.rinha.backend.bank.TransactionResult.NotEnoughAccountBalance
import br.com.rinha.backend.bank.accounts.domain.TransactionDefinition
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route


fun Route.clients() {
    route("/clientes") {
        statement()
        transactions()
    }
}

fun Route.transactions() {
    post("/{idCliente}/transacoes") {

        val clientId = call.parameters["idCliente"]!!.toInt()

        val transaction = call.runCatching { receive<TransactionDefinition>() }.getOrNull()
        if (transaction == null || !transaction.isValid()) {
            call.respond(HttpStatusCode.UnprocessableEntity)
            return@post
        }

        when (val result = Accounts.addTransaction(clientId, transaction)) {
            is ClientNotFound -> call.respond(HttpStatusCode.NotFound)
            is NotEnoughAccountBalance -> call.respond(HttpStatusCode.UnprocessableEntity)
            is TransactionResult.Succeed -> call.respond(HttpStatusCode.OK, result.balance)
        }
    }
}

fun Route.statement() {
    get("{idCliente}/extrato") {
        call.respond(
            Accounts.getStatementFor(call.parameters["idCliente"]!!.toInt())
        )
    }
}
