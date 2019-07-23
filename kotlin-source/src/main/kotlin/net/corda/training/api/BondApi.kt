package net.corda.training.api

import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.toX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.NodeInfo
import net.corda.core.utilities.loggerFor
import net.corda.finance.contracts.asset.Cash
import net.corda.finance.workflows.getCashBalances
import net.corda.training.flow.*
import net.corda.training.state.BondState
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.slf4j.Logger
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * This API is accessible from /api/iou. The endpoint paths specified below are relative to it.
 * We've defined a bunch of endpoints to deal with IOUs, cash and the various operations you can perform with them.
 */
@Path("iou")
class BondApi(val rpcOps: CordaRPCOps) {
    private val me = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<BondApi>()
    }

    fun X500Name.toDisplayString() : String  = BCStyle.INSTANCE.toString(this)

    /** Helpers for filtering the network map cache. */
    private fun isNotary(nodeInfo: NodeInfo) = rpcOps.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }
    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == me
    private fun isNetworkMap(nodeInfo : NodeInfo) = nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to me.toString())

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<String>> {
        return mapOf("peers" to rpcOps.networkMapSnapshot()
                .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
                .map { it.legalIdentities.first().name.toX500Name().toDisplayString() })
    }

    /**
     * Task 1
     * Displays all IOU states that exist in the node's vault.
     * TODO: Return a list of IOUStates on ledger
     * Hint - Use [rpcOps] to query the vault all unconsumed [BondState]s
     */
    @GET
    @Path("ious")
    @Produces(MediaType.APPLICATION_JSON)
    fun getIOUs(): List<StateAndRef<ContractState>> {
        // Filter by state type: IOU.
        return rpcOps.vaultQueryBy<BondState>().states
    }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GET
    @Path("cash")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCash(): List<StateAndRef<ContractState>> {
        // Filter by state type: Cash.
        return rpcOps.vaultQueryBy<Cash.State>().states
    }

    /**
     * Displays all cash states that exist in the node's vault.
     */
    @GET
    @Path("cash-balances")
    @Produces(MediaType.APPLICATION_JSON)
    // Display cash balances.
    fun getCashBalances() = rpcOps.getCashBalances()

    @POST
    @Path("issue-bond")
    fun issueBond (@QueryParam(value = "bondName") name: String,
                   @QueryParam(value = "amount") amount: Int,
                   @QueryParam(value = "pricePerUnit") unit: Int,
                   @QueryParam(value = "duration") duration: Int,
                   @QueryParam(value = "interestRate") interestRate: Double):
            Response {

        val me = rpcOps.nodeInfo().legalIdentities.first()
        val total = (amount*unit)
        // get current date.
        val currentDate = Calendar.getInstance()
        // change date format to dd/MM/yyyy
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val issueDate = dateFormat.format(currentDate.getTime())
        // set maturity date from duration of bond.
        currentDate.add(Calendar.YEAR, duration)
        val maturityDate = dateFormat.format(currentDate.getTime())

        try {
            val bondState = BondState(me, me, name, duration, total, amount, unit, issueDate, maturityDate, interestRate, UniqueIdentifier())
            rpcOps.startFlow(::BondIssueFlow, bondState).returnValue.get()
            return Response.status(Response.Status.CREATED).entity("Issue Bond ${bondState} Successfully").build()

        } catch (e: Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .build()
        }
    }

    @GET
    @Path("transfer-bond")
    fun transferBond (@QueryParam(value = "party") party: String,
                      @QueryParam(value = "amount") amount: Int): Response {
        val holderParty = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(party)) ?: throw IllegalArgumentException("Unknown party name.")
        try {
            rpcOps.startFlow(::BondTransferFlow, UniqueIdentifier(), holderParty, amount).returnValue.get()
            return Response.status(Response.Status.CREATED).entity("Transfer Bond Amount ${amount} to ${holderParty} Successfully.").build()

        } catch (e: Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.message)
                    .build()
        }
    }
//    /**
//     * Initiates a flow to agree an IOU between two parties.
//     * Example request:
//     * curl -X PUT 'http://localhost:10007/api/iou/issue-iou?amount=99&currency=GBP&party=O=ParticipantC,L=New%20York,C=US
//     */
//    @PUT
//    @Path("issue-iou")
//    fun issueIOU(@QueryParam(value = "amount") amount: Int,
//                 @QueryParam(value = "currency") currency: String,
//                 @QueryParam(value = "party") party: String): Response {
//        // Get party objects for myself and the counterparty.
//        val me = rpcOps.nodeInfo().legalIdentities.first()
//        val lender = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(party)) ?: throw IllegalArgumentException("Unknown party name.")
//        // Create a new IOU state using the parameters given.
//        try {
//            val state = BondState(Amount(amount.toLong() * 100, Currency.getInstance(currency)), lender, me)
//            // Start the IOUIssueFlow. We block and waits for the flow to return.
//            val result = rpcOps.startTrackedFlow(::IOUIssueFlow, state).returnValue.get()
//            // Return the response.
//            return Response
//                    .status(Response.Status.CREATED)
//                    .entity("Transaction id ${result.id} committed to ledger.\n${result.tx.outputs.single()}")
//                    .build()
//        // For the purposes of this demo app, we do not differentiate by exception type.
//        } catch (e: Exception) {
//            return Response
//                    .status(Response.Status.BAD_REQUEST)
//                    .entity(e.message)
//                    .build()
//        }
//    }
//
//    /**
//     * Transfers an IOU specified by [linearId] to a new party.
//     */
//    @GET
//    @Path("transfer-iou")
//    fun transferIOU(@QueryParam(value = "id") id: String,
//                    @QueryParam(value = "party") party: String): Response {
//        val linearId = UniqueIdentifier.fromString(id)
//        val newLender = rpcOps.wellKnownPartyFromX500Name(CordaX500Name.parse(party)) ?: throw IllegalArgumentException("Unknown party name.")
//        try {
//            rpcOps.startFlow(::IOUTransferFlow, linearId, newLender).returnValue.get()
//            return Response.status(Response.Status.CREATED).entity("IOU $id transferred to $party.").build()
//
//        } catch (e: Exception) {
//            return Response
//                    .status(Response.Status.BAD_REQUEST)
//                    .entity(e.message)
//                    .build()
//        }
//    }
//
//    /**
//     * Settles an IOU. Requires cash in the right currency to be able to settle.
//     * Example request:
//     * curl -X PUT 'http://localhost:10007/api/iou/issue-iou?amount=99&currency=GBP&party=O=ParticipantC,L=New%20York,C=US
//     */
//    @GET
//    @Path("settle-iou")
//    fun settleIOU(@QueryParam(value = "id") id: String,
//                  @QueryParam(value = "amount") amount: Int,
//                  @QueryParam(value = "currency") currency: String): Response {
//        val linearId = UniqueIdentifier.fromString(id)
//        val settleAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))
//
//        try {
//            rpcOps.startFlow(::IOUSettleFlow, linearId, settleAmount).returnValue.get()
//            return Response.status(Response.Status.CREATED).entity("$amount $currency paid off on IOU id $id.").build()
//
//        } catch (e: Exception) {
//            return Response
//                    .status(Response.Status.BAD_REQUEST)
//                    .entity(e.message)
//                    .build()
//        }
//    }
//
//    /**
//     * Helper end-point to issue some cash to ourselves.
//     */
//    @GET
//    @Path("self-issue-cash")
//    fun selfIssueCash(@QueryParam(value = "amount") amount: Int,
//                      @QueryParam(value = "currency") currency: String): Response {
//        val issueAmount = Amount(amount.toLong() * 100, Currency.getInstance(currency))
//
//        try {
//            val cashState = rpcOps.startFlow(::SelfIssueCashFlow, issueAmount).returnValue.get()
//            return Response.status(Response.Status.CREATED).entity(cashState.toString()).build()
//
//        } catch (e: Exception) {
//            return Response
//                    .status(Response.Status.BAD_REQUEST)
//                    .entity(e.message)
//                    .build()
//        }
//    }
}