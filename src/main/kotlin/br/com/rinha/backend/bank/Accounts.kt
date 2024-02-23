package br.com.rinha.backend.bank

import br.com.rinha.backend.bank.accounts.domain.AccountStatement
import br.com.rinha.backend.bank.accounts.domain.SimpleAccountBalance
import br.com.rinha.backend.bank.accounts.domain.TransactionDefinition
import br.com.rinha.backend.bank.accounts.entities.TransactionEntity
import br.com.rinha.backend.bank.accounts.entities.query.QClientAccountEntity
import br.com.rinha.backend.bank.accounts.entities.query.QTransactionEntity
import br.com.rinha.backend.bank.accounts.tx.CoroutineTransactionalOperator
import io.ebean.DB
import io.ebeaninternal.api.SpiEbeanServer
import io.ktor.server.plugins.NotFoundException
import io.opentelemetry.instrumentation.annotations.WithSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

private val CLIENT_NOT_FOUND_CODE = -1 to null
private val NOT_ENOUGH_ACCOUNT_BALANCE_CODE = -2 to null

sealed class TransactionResult {
    data class Succeed(val balance: SimpleAccountBalance) : TransactionResult()
    data object ClientNotFound : TransactionResult()
    data object NotEnoughAccountBalance : TransactionResult()
}

object Accounts {

    private val txOperator = CoroutineTransactionalOperator(DB.getDefault() as SpiEbeanServer)

    suspend fun addTransaction(clientId: Int, transaction: TransactionDefinition): TransactionResult {

        val balance = txOperator.execute { add(clientId, transaction.normalized()) }

        return when (balance.saldo to balance.limite) {
            CLIENT_NOT_FOUND_CODE -> TransactionResult.ClientNotFound
            NOT_ENOUGH_ACCOUNT_BALANCE_CODE -> TransactionResult.NotEnoughAccountBalance
            else -> TransactionResult.Succeed(balance)
        }
    }

    @WithSpan
    private suspend fun add(idClient: Int, transaction: TransactionDefinition): SimpleAccountBalance {
        val query =
            DB.sqlQuery("select saldo, limite from criartransacao(?,?,?) as f(saldo integer, limite integer)").apply {
                setParameter(1, idClient)
                setParameter(2, transaction.value)
                setParameter(3, transaction.description)
            }

        return withContext(Dispatchers.IO) {
            query.findOne()!!.run {
                SimpleAccountBalance(
                    limite = get("limite")?.run { this as Int * -1 },
                    saldo = getInteger("saldo")
                )
            }
        }
    }

    @WithSpan
    suspend fun getStatementFor(clientId: Int): AccountStatement = txOperator.execute {
        withContext(Dispatchers.IO) {
            val accountDeferred = findClientAccountFor(this, clientId)
            val lastTransactionsDeferred = loadLastTransactionsFor(this, clientId)

            val account = accountDeferred.await() ?: throw NotFoundException()

            AccountStatement(
                balance = account.toAccountBalance(),
                lastTransactions = lastTransactionsDeferred.await().map { it.toTransaction() }
            )
        }
    }

    @WithSpan
    private fun loadLastTransactionsFor(
        scope: CoroutineScope,
        clientId: Int
    ): Deferred<List<TransactionEntity>> {
        return scope.async {
            QTransactionEntity()
                .clientId.eq(clientId)
                .createAt.desc()
                .setMaxRows(10)
                .findList()
        }
    }

    @WithSpan
    private fun findClientAccountFor(scope: CoroutineScope, clientId: Int) =
        scope.async { QClientAccountEntity().id.eq(clientId).findOne() }
}