package com.rohitthebest.manageyourrenters.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.entity.Renter

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
    }
}