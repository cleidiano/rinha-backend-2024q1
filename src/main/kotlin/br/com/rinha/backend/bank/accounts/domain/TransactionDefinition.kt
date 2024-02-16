package br.com.rinha.backend.bank.accounts.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TransactionDefinition(
    @SerialName("valor")
    val value: Int,

    @SerialName("tipo")
    val type: Char,

    @SerialName("descricao")
    val description: String
) {
    fun validated(): TransactionDefinition {
        check(value > 0) { "valor da transação inválido" }
        check(type == 'c' || type == 'd') { "tipo de transação inválida" }
        check(description.isNotBlank() && description.length <= 10) { "descrição inválida" }

        if (this.type == 'c')
            return this

        return this.copy(value = value * -1)
    }
}