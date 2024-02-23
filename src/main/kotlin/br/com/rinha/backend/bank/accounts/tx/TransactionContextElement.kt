package br.com.rinha.backend.bank.accounts.tx

import io.ebeaninternal.api.SpiTransaction
import io.ebeaninternal.server.transaction.TransactionScopeManager
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

class TransactionContextElement(
    private val transactionManager: TransactionScopeManager,
    private val spiTransaction: SpiTransaction = transactionManager.active()
) : ThreadContextElement<SpiTransaction?> {

    override val key = TransactionContextElement

    override fun restoreThreadContext(context: CoroutineContext, oldState: SpiTransaction?) {
        runCatching {
            transactionManager.clearExternal()
            if (oldState != null && oldState.isActive) {
                transactionManager.replace(oldState)
            }
        }
    }

    override fun updateThreadContext(context: CoroutineContext): SpiTransaction? {
        return if (spiTransaction.isActive) setupTransactionInThreadContext() else null
    }

    private fun setupTransactionInThreadContext(): SpiTransaction? {
        return runCatching {
            val oldState = transactionManager.inScope()
            transactionManager.clearExternal()
            transactionManager.replace(spiTransaction)
            oldState
        }.getOrNull()
    }

    companion object TransactionContext : CoroutineContext.Key<TransactionContextElement>
}
