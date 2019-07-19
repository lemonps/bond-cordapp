package net.corda.training.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.training.contract.BondContract
import net.corda.training.state.BondState
import java.util.Collections.singletonList

/**
 * This is the flow which handles issuance of new IOUs on the ledger.
 * Gathering the counterparty's signature is handled by the [CollectSignaturesFlow].
 * Notarisation (if required) and commitment to the ledger is handled by the [FinalityFlow].
 * The flow returns the [SignedTransaction] that was committed to the ledger.
 */
@InitiatingFlow
@StartableByRPC
class BondIssueFlow(val state: BondState): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Step 2. Create a new issue command.
        // Remember that a command is a CommandData object and a list of CompositeKeys
        val issueCommand = Command(BondContract.Commands.Issue(), state.issuer.owningKey )

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        builder.addOutputState(state, BondContract.IOU_CONTRACT_ID)
        builder.addCommand(issueCommand)

        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val session = initiateFlow(ourIdentity)
//        // Step 6. Collect the other party's signature using the SignTransactionFlow.
//        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction.
        return subFlow(FinalityFlow(ptx, emptyList()))
    }
}

/**
 * This is the flow which signs IOU issuances.
 * The signing is handled by the [SignTransactionFlow].
 */
//@InitiatedBy(BondIssueFlow::class)
//class BondIssueFlowResponder(val flowSession: FlowSession): FlowLogic<SignedTransaction>() {
//
//    @Suspendable
//    override fun call(): SignedTransaction {
//        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
//            override fun checkTransaction(stx: SignedTransaction) = requireThat {
//                val output = stx.tx.outputs.single().data
//                "This must be an IOU transaction" using (output is BondState)
//            }
//        }
//
//        val txWeJustSignedId = subFlow(signedTransactionFlow)
//
//        return subFlow(ReceiveFinalityFlow(otherSideSession = flowSession, expectedTxId = txWeJustSignedId.id))
//    }
//}