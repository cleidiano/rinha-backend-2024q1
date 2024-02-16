package br.com.rinha.backend.plugins

import br.com.rinha.backend.bank.Accounts
import br.com.rinha.backend.bank.accounts.domain.TransactionDefinition
import br.com.rinha.backend.bank.TransactionResult
import br.com.rinha.backend.bank.TransactionResult.ClientNotFound
import br.com.rinha.backend.bank.TransactionResult.NotEnoughAccountBalance
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.clients() {
    route("/clientes") {
        statement()
        transactions()
    }
}

fun Route.transactions() {
    post("/{idCliente}/transacoes") {

        val clientId = call.parameters["idCliente"]!!.toInt()
        when (val result = Accounts.addTransaction(clientId, call.receive<TransactionDefinition>())) {
            is ClientNotFound -> call.respond(HttpStatusCode.NotFound)
            is NotEnoughAccountBalance -> call.respond(HttpStatusCode.UnprocessableEntity)
            is TransactionResult.Succeed -> call.respond(HttpStatusCode.Accepted, result.balance)
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
