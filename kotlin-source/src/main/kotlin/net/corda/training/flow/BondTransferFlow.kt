package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.training.contract.BondContract
import net.corda.training.state.BondState

/**
 * This is the flow which handles transfers of existing IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class BondTransferFlow(val linearId: UniqueIdentifier,
                      val holderParty: Party,
                      val amount: Int): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // Stage 1. Retrieve Bond specified by linearId from the vault.
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(linearId))
        val bondStateAndRef =  serviceHub.vaultService.queryBy<BondState>(queryCriteria).states.single()
        val inputBond = bondStateAndRef.state.data

        // Stage 2. This flow can only be initiated by the current recipient.
        if (ourIdentity != inputBond.issuer) {
            throw IllegalArgumentException("Bond transfer can only be initiated by the Bond Agent.")
        }

        // Stage 3. Set the new Bond state reflecting a new owner.
        // val outputAvailableBond = inputBond.availableBond(amount)
        val outputTransferBond = inputBond.transferBond(holderParty, amount)


        // Stage 4. Create the transfer command.
        val signers = (inputBond.participants + holderParty).map { it.owningKey }
        val transferCommand = Command(BondContract.Commands.TransferBond(), signers)

        // Stage 5. Get a reference to a transaction builder.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        val builder = TransactionBuilder(notary = notary)

        // Stage 6. Create the transaction which comprises one input, one output and one command.
        builder.withItems(bondStateAndRef,
                StateAndContract(outputTransferBond, BondContract.BOND_CONTRACT_ID),
                transferCommand)

        // Stage 7. Verify and sign the transaction.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Stage 8. Collect signature from borrower and the new lender and add it to the transaction.
        // This also verifies the transaction and checks the signatures.
        val sessions = (inputBond.participants - ourIdentity + holderParty).map { initiateFlow(it) }.toSet()
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Stage 9. Notarise and record the transaction in our vaults.
        return subFlow(FinalityFlow(stx, sessions))
    }
}

/**
 * This is the flow which signs IOU transfers.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(BondTransferFlow::class)
class BondTransferFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an IOU transaction" using (output is BondState)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
    }
}