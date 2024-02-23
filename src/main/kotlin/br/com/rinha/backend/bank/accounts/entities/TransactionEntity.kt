package br.com.rinha.backend.bank.accounts.entities

import br.com.rinha.backend.bank.accounts.domain.Transaction
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "transacao")
class TransactionEntity(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column(name = "valor")
    var value: Int,

    @Column(name = "descricao")
    var description: String,

    @Column(name = "realizadaem")
    var createAt: Instant,

    @Column(name = "idcliente")
    var clientId: Int
) {

    fun toTransaction() = Transaction(
        type = if (value > 0) 'c' else 'd',
        value = if (this.value < 0) this.value * -1 else this.value,
        createdAt = this.createAt,
        description = this.description
    )
}