package com.rohitthebest.manageyourrenters.database.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.isValid

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    var amount: Double,
    var created: Long,
    var modified: Long,
    var spentOn: String,
    var uid: String,
    var key: String,
    var categoryKey: String = "",
    var paymentMethods: List<String>? = null,
    var isSynced: Boolean = true
) {

    constructor() : this(
        null,
        0.0,
        0L,
        0L,
        "",
        "",
        "",
        "",
        null,
        true
    )

    fun showDetailedInfoInAlertDialog(
        context: Context,
        categoryName: String,
        paymentMethodsMap: Map<String, String>
    ) {

        val spentOn = if (this.spentOn.isValid()) this.spentOn else categoryName
        val msg =
            "\nDate : ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    this.created, "dd-MM-yyyy hh:mm a"
                )
            }\n\n" +
                    "Amount: ${this.amount}\n\n" +
                    "Spent On: ${spentOn}\n\n" +
                    "Category: $categoryName" +
                    "\n\n" +
                    "PaymentMethods: ${getPaymentMethodString(paymentMethodsMap)}"

        MaterialAlertDialogBuilder(context)
            .setTitle("Expense info")
            .setMessage(msg)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }

    /**
     * @param paymentMethodsMap : map of paymentMethodKey as key and paymentMethod as value
     * @return all the payment methods used in this expense
     */
    fun getPaymentMethodString(paymentMethodsMap: Map<String, String>): String {

        return if (this.paymentMethods == null) {
            Constants.OTHER_PAYMENT_METHOD
        } else {
            var paymentMethods = ""
            this.paymentMethods!!.forEachIndexed { index, pm ->

                if (paymentMethodsMap[pm] != null) {
                    paymentMethods += paymentMethodsMap[pm]
                }

                if (index != this.paymentMethods!!.size - 1) {
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