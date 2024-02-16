package br.com.rinha.backend.bank.accounts.domain

import br.com.rinha.backend.plugins.serde.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class SimpleAccountBalance(val limite: Int?, val saldo: Int)

@Serializable
data class AccountStatement(
    @SerialName("saldo")
    val balance: AccountBalance,

    @SerialName("ultimas_transacoes")
    val lastTransactions: List<Transaction>
)

@Serializable
data class AccountBalance(

    @SerialName("total")
    val total: Int,

    @SerialName("limite")
    val accountOverdraft: Int,

    @SerialName("data_extrato")
    @Serializable(with = InstantSerializer::class)
    val statementDateTime: Instant,
)

@Serializable
data class Transaction(
    @SerialName("valor")
    val value: Int,

    @SerialName("tipo")
    val type: Char,

    @SerialName("descricao")
    val description: String,

    @SerialName("realizada_em")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant
)


