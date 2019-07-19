package net.corda.training.Schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for BondState.
 */
object BondSchema

/**
 * An BondState schema.
 */
object BondSchemaV1 : MappedSchema(
        schemaFamily = BondSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentBond::class.java)) {
    @Entity
    @Table(name = "bond-state")
    class PersistentBond(
            @Column(name = "issuer")
            var issuer: String,

            @Column(name = "owner")
            var owner: String,

            @Column(name = "bondName")
            var bondName: String,

            @Column(name = "duration")
            var duration: Int,

            @Column(name = "total")
            var total: Int,

            @Column(name = "amount")
            var amount: Int,

            @Column(name = "unit")
            var unit: Int,

            @Column(name = "issue-date")
            var issueDate: String,

            @Column(name = "maturity-date")
            var maturityDate: String,

            @Column(name = "interest-rate")
            var interestRate: Double,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("","","", 0, 0, 0, 0, "", "", 0.0, UUID.randomUUID())
    }
}