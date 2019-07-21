package net.corda.training.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.contracts.utils.sumCash
import net.corda.training.state.BondState

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
        val IOU_CONTRACT_ID = "net.corda.training.contract.BondContract"
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {
        class IssueBond : TypeOnlyCommandData(), Commands
        class TransferBond : TypeOnlyCommandData(), Commands
    }

    /**
     * The contract code for the [BondContract].
     * The constraints are self documenting so don't require any additional explanation.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<BondContract.Commands>()
        when (command.value) {
            is Commands.IssueBond -> requireThat {

            }
            is Commands.TransferBond -> requireThat {

            }
        }
    }
}
