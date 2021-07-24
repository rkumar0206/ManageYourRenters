package com.rohitthebest.manageyourrenters.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.database.model.*

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

fun fromBorrowerPaymentToString(borrowerPayment: BorrowerPayment): String {

    return Gson().toJson(borrowerPayment)
}

fun fromStringToBorrowerPayment(str: String): BorrowerPayment {

    return Gson().fromJson(str, object : TypeToken<BorrowerPayment>() {}.type)
}

fun fromPartialPaymentListToString(list: List<PartialPayment>): String {

    return gson.toJson(list)
}

fun fromStringToPartialPaymentList(jsonString: String): List<PartialPayment> {

    val type = object : TypeToken<List<PartialPayment>>() {}.type
    return gson.fromJson(jsonString, type)
}
