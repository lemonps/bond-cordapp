package net.corda.training.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.training.contract.BondContract
import java.util.*

/**
 * The IOU State object, with the following properties:
 * - [amount] The amount owed by the [borrower] to the [lender]
 * - [lender] The lending party.
 * - [borrower] The borrowing party.
 * - [contract] Holds a reference to the [BondContract]
 * - [paid] Records how much of the [amount] has been paid.
 * - [linearId] A unique id shared by all LinearState states representing the same agreement throughout history within
 *   the vaults of all parties. Verify methods should check that one input and one output share the id in a transaction,
 *   except at issuance/termination.
 */
@BelongsToContract(BondContract::class)
data class BondState(val issuer: Party,
                     val borrower: Party,
                     val bondName: String,
                     val duration: Int,
                     val total: Int,
                     val amount: Int,
                     val unit: Int,
                     val issueDate: String,
                     val maturityDate: String,
                     val interestRate: Double,
                     override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction. In this case, the
     *  lender or the borrower.
     */
    override val participants: List<Party> get() = listOf(issuer, borrower)
}