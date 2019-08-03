package net.corda.training.state

import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.training.contract.BondContract
import java.util.*

@BelongsToContract(BondContract::class)
data class UserState(
                     val name: String,
                     val bondName: String,
                     val owner: Party,
                     val amount: Int,
                     val total: Int,
//                     val paid: Amount<Currency> = Amount(0, total.token),
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
                     override val participants: List<Party> get() = listOf(owner)
//                     fun pay(amountToPay: Amount<Currency>) = copy(paid = paid.plus(amountToPay))
}