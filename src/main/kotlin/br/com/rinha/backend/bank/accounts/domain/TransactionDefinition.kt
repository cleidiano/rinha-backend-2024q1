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
    fun normalized(): TransactionDefinition {

        if (this.type == 'c')
            return this

        return this.copy(value = value * -1)
    }

    fun isValid(): Boolean {
        if (value <= 0) return false
        if (type != 'c' && type != 'd') return false

        return description.isNotBlank() && description.length <= 10
    }

}