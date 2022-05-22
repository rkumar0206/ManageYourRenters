package com.rohitthebest.manageyourrenters.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.database.model.*

private val gson = Gson()

fun convertRenterToJSONString(renter: Renter): String {

    return renter.convertToJsonString() ?: ""
}

fun convertJSONtoRenter(jsonString: String?): Renter {

    return jsonString?.convertJsonToObject(Renter::class.java)!!
}

fun convertStringListToJSON(list: List<String?>): String {

    return gson.toJson(list)
}

fun convertJSONToStringList(jsonString: String?): List<String?> {

    val type = object : TypeToken<List<String?>>() {}.type
    return gson.fromJson(jsonString, type)
}

fun fromBorrowerToString(borrower: Borrower): String {

    return borrower.convertToJsonString() ?: ""
}

fun fromStringToBorrower(str: String): Borrower {

    return str.convertJsonToObject(Borrower::class.java)!!
}

fun fromBorrowerPaymentToString(borrowerPayment: BorrowerPayment): String {

    return borrowerPayment.convertToJsonString() ?: ""
}

fun fromStringToBorrowerPayment(str: String): BorrowerPayment {

    return str.convertJsonToObject(BorrowerPayment::class.java)!!
}

fun fromPartialPaymentListToString(list: List<PartialPayment>): String {

    return gson.toJson(list)
}

fun fromStringToPartialPaymentList(jsonString: String): List<PartialPayment> {

    val type = object : TypeToken<List<PartialPayment>>() {}.type
    return gson.fromJson(jsonString, type)
}

fun fromEMIToString(emi: EMI): String {

    return emi.convertToJsonString() ?: ""
}

fun fromStringToEMI(str: String): EMI {

    return str.convertJsonToObject(EMI::class.java)!!
}

fun fromEMIPaymentToString(emiPayment: EMIPayment): String {

    return emiPayment.convertToJsonString() ?: ""
}

fun fromStringToEMIPayment(str: String): EMIPayment {

    return str.convertJsonToObject(EMIPayment::class.java)!!
}
