package br.com.rinha.backend.bank.accounts.tx

import io.ebeaninternal.api.SpiEbeanServer
import io.ebeaninternal.api.SpiTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoroutineTransactionalOperator(
    private val ebeanServer: SpiEbeanServer,
) {

    suspend fun <T> execute(block: suspend CoroutineScope.() -> T): T =
        withContext(Dispatchers.IO) {
            ebeanServer.beginTransaction().use { transaction ->
                val transactionScopeManager = ebeanServer.transactionManager().scope()
                transactionScopeManager.clearExternal()
                withContext(TransactionContextElement(transactionScopeManager, transaction as SpiTransaction)) {
                    block().also { transaction.commit() }
                }
            }
        }
}
