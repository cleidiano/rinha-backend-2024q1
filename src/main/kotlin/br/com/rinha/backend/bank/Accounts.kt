package br.com.rinha.backend.bank

import br.com.rinha.backend.bank.accounts.domain.AccountStatement
import br.com.rinha.backend.bank.accounts.domain.SimpleAccountBalance
import br.com.rinha.backend.bank.accounts.domain.TransactionDefinition
import br.com.rinha.backend.bank.accounts.entities.TransactionEntity
import br.com.rinha.backend.bank.accounts.entities.query.QClientAccountEntity
import br.com.rinha.backend.bank.accounts.entities.query.QTransactionEntity
import io.ebean.DB
import io.ebean.Transaction
import io.ktor.server.plugins.*
import kotlinx.coroutines.*

private val CLIENT_NOT_FOUND_CODE = -1 to null
private val NOT_ENOUGH_ACCOUNT_BALANCE_CODE = -2 to null

sealed class TransactionResult {
    data class Succeed(val balance: SimpleAccountBalance) : TransactionResult()
    data object ClientNotFound : TransactionResult()
    data object NotEnoughAccountBalance : TransactionResult()
}

object Accounts {

    suspend fun addTransaction(clientId: Int, transaction: TransactionDefinition): TransactionResult {
        val balance = add(clientId, transaction.validated())

        return when (balance.saldo to balance.limite) {
            CLIENT_NOT_FOUND_CODE -> TransactionResult.ClientNotFound
            NOT_ENOUGH_ACCOUNT_BALANCE_CODE -> TransactionResult.NotEnoughAccountBalance
            else -> TransactionResult.Succeed(balance)
        }
    }

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
                    limite = getInteger("limite"),
                    saldo = getInteger("saldo")
                )
            }
        }
    }

    suspend fun getStatementFor(clientId: Int): AccountStatement = withContext(Dispatchers.IO) {
        DB.beginTransaction().use { dbTransaction ->
            val accountDeferred = dbTransaction.findClientAccountFor(this, clientId)
            val lastTransactionsDeferred = dbTransaction.loadLastTransactionsFor(this, clientId)

            val account = accountDeferred.await() ?: throw NotFoundException()

            AccountStatement(
                balance = account.toAccountBalance(),
                lastTransactions = lastTransactionsDeferred.await().map { it.toTransaction() }
            )
        }
    }

    private fun Transaction.loadLastTransactionsFor(
        scope: CoroutineScope,
        clientId: Int
    ): Deferred<List<TransactionEntity>> {
        return scope.async {
            QTransactionEntity(this@loadLastTransactionsFor)
                .clientId.eq(clientId)
                .createAt.desc()
                .setMaxRows(10)
                .findList()
        }
    }

    private fun Transaction.findClientAccountFor(scope: CoroutineScope, clientId: Int) =
        scope.async { QClientAccountEntity(this@findClientAccountFor).id.eq(clientId).findOne() }
}