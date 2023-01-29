package com.rohitthebest.manageyourrenters.database.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

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

    fun showDetailedInfoInAlertDialog(context: Context, categoryName: String) {

        val msg =
            "\nDate : ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    this.created, "dd-MM-yyyy hh:mm a"
                )
            }\n\n" +
                    "Amount : ${this.amount}\n\n" +
                    "Spent On : ${this.spentOn}\n\n" +
                    "Category : $categoryName"

        MaterialAlertDialogBuilder(context)
            .setTitle("Expense info")
            .setMessage(msg)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }

}