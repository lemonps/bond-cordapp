package net.corda.training.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.utils.sumCash
import net.corda.training.state.BondState
import net.corda.training.state.UserState

/**
 * The BondContract can handle three transaction types involving [BondState]s.
 * - Issuance: Issuing a new [BondState] on the ledger, which is a bilateral agreement between two parties.
 * - Transfer: Re-assigning the lender/beneficiary.
 * - Settle: Fully or partially settling the [BondState] using the Corda [Cash] contract.
 *
 * LegalProseReference: this is just a dummy string for the time being.
 */
@LegalProseReference(uri = "<prose_contract_uri>")
class BondContract : Contract {
    companion object {
        @JvmStatic
        val BOND_CONTRACT_ID = "net.corda.training.contract.BondContract"
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {
        class IssueBond : TypeOnlyCommandData(), Commands
        class TransferBond : TypeOnlyCommandData(), Commands
        class UserIssue : TypeOnlyCommandData(), Commands
    }

    /**
     * The contract code for the [BondContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<BondContract.Commands>()
        when (command.value) {
            is Commands.IssueBond -> requireThat {
                "No inputs should be consumed when issuing an Bond." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an Bond." using (tx.outputs.size == 1)
                val bond = tx.outputStates.single() as BondState
                "Bond amount must be positive." using (bond.amount > 0)
                "Bond unit (Price Per Unit) must be positive." using (bond.unit > 0)
                "Bond duration must be positive." using (bond.duration > 0)
                "Bond Interest Rate must be positive." using (bond.interestRate > 0)
                "The issuer and owner must have the same identity." using (bond.issuer == bond.owner)
            }
            is Commands.TransferBond -> requireThat { "An IOU transfer transaction should only consume one input state." using (tx.inputs.size == 1)
                "A Bond transfer transaction should only create one output state." using (tx.outputs.size == 1)
                val input = tx.inputStates.single() as BondState
                val output = tx.outputStates.single() as BondState
                "Only the lender property may change." using (input == output.withNewOwner(input.owner))
                "The lender property must change in a transfer." using (input.issuer != output.owner)

            }
            is Commands.UserIssue -> requireThat {
                "No inputs should be consumed when issuing an Bond." using (tx.inputs.isEmpty())
                "Only one output state should be created when issuing an Bond." using (tx.outputs.size == 1)
                val user = tx.outputStates.single() as UserState
                "Bond amount must be positive." using (user.amount > 0)

            }
        }
    }
}
