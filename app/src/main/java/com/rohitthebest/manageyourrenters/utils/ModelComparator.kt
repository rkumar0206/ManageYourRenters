package com.rohitthebest.manageyourrenters.utils

import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.RenterPayment

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
    if (oldData.isSupportingDocAdded != newData.isSupportingDocAdded) map["supportingDocAdded"] =
        newData.isSupportingDocAdded
    if (oldData.supportingDocument != newData.supportingDocument) map["supportingDocument"] =
        newData.supportingDocument

    return map
}

fun compareRenterPaymentModel(
    oldData: RenterPayment,
    newData: RenterPayment
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.currencySymbol != newData.currencySymbol) map["currencySymbol"] =
        newData.currencySymbol
    if (oldData.billPeriodInfo != newData.billPeriodInfo) map["billPeriodInfo"] =
        newData.billPeriodInfo
    if (oldData.isElectricityBillIncluded != newData.isElectricityBillIncluded) map["electricityBillIncluded"] =
        newData.isElectricityBillIncluded
    if (oldData.electricityBillInfo != newData.electricityBillInfo) map["electricityBillInfo"] =
        newData.electricityBillInfo
    if (oldData.houseRent != newData.houseRent) map["houseRent"] = newData.houseRent
    if (oldData.parkingRent != newData.parkingRent) map["parkingRent"] = newData.parkingRent
    if (oldData.netDemand != newData.netDemand) map["netDemand"] = newData.netDemand
    if (oldData.amountPaid != newData.amountPaid) map["amountPaid"] = newData.amountPaid
    if (oldData.extras != newData.extras) map["extras"] = newData.extras
    if (oldData.note != newData.note) map["note"] = newData.note
    if (oldData.isSupportingDocAdded != newData.isSupportingDocAdded) map["supportingDocAdded"] =
        newData.isSupportingDocAdded
    if (oldData.supportingDocument != newData.supportingDocument) map["supportingDocument"] =
        newData.supportingDocument

    return map
}