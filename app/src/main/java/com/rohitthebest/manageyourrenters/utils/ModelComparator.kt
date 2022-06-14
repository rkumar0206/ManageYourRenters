package com.rohitthebest.manageyourrenters.utils

import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment

/**
 * This class helps to compare the main fields of
 * old and new model and return a map of unmatched fields
 */

fun compareBorrowerPaymentModel(
    oldData: BorrowerPayment,
    newData: BorrowerPayment
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.currencySymbol != newData.currencySymbol) map["currencySymbol"] =
        newData.currencySymbol
    if (oldData.amountTakenOnRent != newData.amountTakenOnRent) map["amountTakenOnRent"] =
        newData.amountTakenOnRent
    if (oldData.dueLeftAmount != newData.dueLeftAmount) map["dueLeftAmount"] = newData.dueLeftAmount
    if (oldData.isDueCleared != newData.isDueCleared) map["dueCleared"] = newData.isDueCleared
    if (oldData.interest != newData.interest) map["interest"] = newData.interest
    if (oldData.messageOrNote != newData.messageOrNote) map["messageOrNote"] = newData.messageOrNote

    return map
}