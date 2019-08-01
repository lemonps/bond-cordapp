package net.corda.training.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.training.contract.BondContract

@BelongsToContract(BondContract::class)
data class UserState(
                     val name: String,
                     val bondName: String,
                     val owner: Party,
                     val amount: Int,
                     val total: Int,
//                     val unit: Int,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {

                        override val participants: List<Party> get() = listOf(owner)
                     }