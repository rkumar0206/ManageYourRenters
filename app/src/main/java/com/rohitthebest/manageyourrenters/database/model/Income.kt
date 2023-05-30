package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.isValid

@Entity(tableName = "income_table")
@IgnoreExtraProperties
data class Income(
    @PrimaryKey(autoGenerate = false) var key: String,
    var created: Long,
    var modified: Long,
    var source: String,
    var income: Double,
    var month: Int,
    var year: Int,
    var monthYearString: String,
    var isSynced: Boolean,
    var uid: String,
    var linkedPaymentMethods: List<String>? = null
) {
    constructor() : this(
        "",
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        0.0,
        0,
        0,
        "",
        false,
        "",
        null
    )

    fun generateMonthYearString(): String {
        return "${this.month}_${this.year}"
    }

    fun getPaymentMethodString(paymentMethodsMap: Map<String, String>): String {

        return if (this.linkedPaymentMethods == null) {
            Constants.OTHER_PAYMENT_METHOD
        } else {
            var paymentMethods = ""
            this.linkedPaymentMethods!!.forEachIndexed { index, pm ->

                if (paymentMethodsMap[pm] != null) {
                    paymentMethods += paymentMethodsMap[pm]
                }

                if (index != this.linkedPaymentMethods!!.size - 1) {
                    paymentMethods += " | "
                }
            }

            if (!paymentMethods.isValid()) {
                paymentMethods = Constants.OTHER_PAYMENT_METHOD
            }

            paymentMethods
        }
    }

}
