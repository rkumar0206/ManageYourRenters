package com.rohitthebest.manageyourrenters.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.model.Renter

class ConversionWithGson {

    companion object {

        private val gson = Gson()

        fun convertRenterToJSONString(renter: Renter): String {

            return gson.toJson(renter)
        }

        fun convertJSONtoRenter(jsonString: String?): Renter {

            val type = object : TypeToken<Renter?>() {}.type
            return gson.fromJson(jsonString, type)
        }

        fun convertPaymentToJSONString(payment: Payment): String {

            return gson.toJson(payment)
        }

        fun convertJSONtoPayment(jsonString: String?): Payment {

            val type = object : TypeToken<Payment?>() {}.type
            return gson.fromJson(jsonString, type)
        }

        fun convertStringListToJSON(list: List<String?>): String {

            return gson.toJson(list)
        }

        fun convertJSONToStringList(jsonString: String?): List<String?> {

            val type = object : TypeToken<List<String?>>() {}.type
            return gson.fromJson(jsonString, type)
        }

        fun fromBorrowerToString(borrower: Borrower): String {

            return gson.toJson(borrower)
        }

        fun fromStringToBorrower(str: String): Borrower {

            return gson.fromJson(str, object : TypeToken<Borrower>() {}.type)
        }

    }
}