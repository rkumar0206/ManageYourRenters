package com.rohitthebest.manageyourrenters.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.rohitthebest.manageyourrenters.data.ParsedImportExportExpense
import java.lang.reflect.Type

class ParsedImportExportExpenseJsonDeserializer : JsonDeserializer<ParsedImportExportExpense?> {

    @Throws(Exception::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ParsedImportExportExpense {

        try {

            val jsonObject = json?.asJsonObject

            val category = if (jsonObject?.get("category") == null) {
                ""
            } else {
                try {
                    jsonObject.get("category")?.asString
                } catch (e: Exception) {
                    ""
                }
            }

            val date = validateDateAndGiveACommonFormat(
                try {
                    jsonObject?.get("date")?.asString ?: ""
                } catch (e: Exception) {
                    ""
                }
            )

            val amount = try {
                jsonObject?.get("amount")?.asDouble
            } catch (e: Exception) {
                validateAmount(
                    try {
                        jsonObject?.get("amount")?.asString ?: ""
                    } catch (e: Exception) {
                        ""
                    }
                )
            }

            val spentOn = try {
                jsonObject?.get("spentOn")?.asString
            } catch (e: Exception) {
                ""
            }
            val paymentMethod = try {
                jsonObject?.get("paymentMethod")?.asString
            } catch (e: Exception) {
                ""
            }

            return ParsedImportExportExpense(
                date = date,
                amount = amount ?: 0.0,
                category = category ?: "",
                spentOn = spentOn,
                paymentMethod = paymentMethod
            )

        } catch (e: Exception) {
            throw e
        }

    }

    private fun validateAmount(amount: String): Double {

        // check if the amount is in number format or not
        // if not in number format return 0.0
        // if amount is null or empty return 0.0

        return if (amount.isNotValid()) {
            0.0
        } else {
            try {
                amount.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        }

    }

    private fun validateDateAndGiveACommonFormat(dateString: String): String? {

        val timeInMillis =
            if (dateString.isNotValid()) System.currentTimeMillis() else WorkingWithDateAndTime.identifyDateAndTimeFormatAndConvertToMillis(
                dateString
            )
        return WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
            timeInMillis,
            "dd-MM-yyyy hh:mm a"
        )
    }
}