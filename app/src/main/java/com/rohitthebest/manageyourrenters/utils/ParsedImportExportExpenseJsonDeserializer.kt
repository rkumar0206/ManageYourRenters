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
            }else {
                jsonObject.get("category")?.asString
            }

            val date = validateDateAndGiveACommonFormat(jsonObject?.get("date")?.asString ?: "")

            val amount = try {
                jsonObject?.get("amount")?.asDouble
            } catch (e: Exception) {
                validateAmount(jsonObject?.get("amount")?.asString ?: "")
            }

            val spentOn = jsonObject?.get("spentOn")?.asString
            val paymentMethod = jsonObject?.get("paymentMethod")?.asString

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