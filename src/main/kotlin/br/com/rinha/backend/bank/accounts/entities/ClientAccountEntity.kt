package br.com.rinha.backend.bank.accounts.entities

import br.com.rinha.backend.bank.accounts.domain.AccountBalance
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "cliente")
@Table(name = "cliente")
class ClientAccountEntity(
    @Id
    var id: Int,

    @Column(name = "saldo")
    var balance: Int,

    @Column(name = "limite")
    var overdraftLimit: Int
) {

    fun toAccountBalance() = AccountBalance(
        total = this.balance,
        statementDateTime = Instant.now(),
        accountOverdraft = this.overdraftLimit
    )

}