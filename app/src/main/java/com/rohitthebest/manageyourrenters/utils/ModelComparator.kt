package com.rohitthebest.manageyourrenters.utils

import com.rohitthebest.manageyourrenters.database.model.*

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

fun compareEmi(
    oldData: EMI,
    newData: EMI
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.currencySymbol != newData.currencySymbol) map["currencySymbol"] =
        newData.currencySymbol
    if (oldData.emiName != newData.emiName) map["emiName"] = newData.emiName
    if (oldData.totalMonths != newData.totalMonths) map["totalMonths"] = newData.totalMonths
    if (oldData.monthsCompleted != newData.monthsCompleted) map["monthsCompleted"] =
        newData.monthsCompleted
    if (oldData.amountPaidPerMonth != newData.amountPaidPerMonth) map["amountPaidPerMonth"] =
        newData.amountPaidPerMonth
    if (oldData.amountPaid != newData.amountPaid) map["amountPaid"] = newData.amountPaid
    if (oldData.isSupportingDocAdded != newData.isSupportingDocAdded) map["supportingDocAdded"] =
        newData.isSupportingDocAdded
    if (oldData.supportingDocument != newData.supportingDocument) map["supportingDocument"] =
        newData.supportingDocument

    return map
}

fun compareEMIPaymentModel(
    oldData: EMIPayment,
    newData: EMIPayment
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.amountPaid != newData.amountPaid) map["amountPaid"] = newData.amountPaid
    if (oldData.fromMonth != newData.fromMonth) map["fromMonth"] = newData.fromMonth
    if (oldData.tillMonth != newData.tillMonth) map["tillMonth"] = newData.tillMonth
    if (oldData.isSupportingDocAdded != newData.isSupportingDocAdded) map["supportingDocAdded"] =
        newData.isSupportingDocAdded
    if (oldData.supportingDocument != newData.supportingDocument) map["supportingDocument"] =
        newData.supportingDocument

    return map
}

fun compareExpenseCategoryModel(
    oldData: ExpenseCategory,
    newData: ExpenseCategory
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.categoryDescription != newData.categoryDescription) map["categoryDescription"] =
        newData.categoryDescription
    if (oldData.categoryName != newData.categoryName) map["categoryName"] = newData.categoryName
    if (oldData.imageUrl != newData.imageUrl) map["imageUrl"] = newData.imageUrl

    return map
}

fun compareExpenseModel(
    oldData: Expense,
    newData: Expense
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.amount != newData.amount) map["amount"] = newData.amount
    if (oldData.spentOn != newData.spentOn) map["spentOn"] = newData.spentOn
    if (oldData.categoryKey != newData.categoryKey) map["categoryKey"] = newData.categoryKey

    return map
}

fun compareMonthlyPaymentModel(
    oldData: MonthlyPayment,
    newData: MonthlyPayment
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.amount != newData.amount) map["amount"] = newData.amount
    if (oldData.monthlyPaymentDateTimeInfo != newData.monthlyPaymentDateTimeInfo) map["monthlyPaymentDateTimeInfo"] =
        newData.monthlyPaymentDateTimeInfo
    if (oldData.categoryKey != newData.categoryKey) map["categoryKey"] = newData.categoryKey
    if (oldData.message != newData.message) map["message"] = newData.message

    return map
}

fun compareMonthlyPaymentCategoryModel(
    oldData: MonthlyPaymentCategory,
    newData: MonthlyPaymentCategory
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.created != newData.created) map["created"] = newData.created
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.categoryDescription != newData.categoryDescription) map["categoryDescription"] =
        newData.categoryDescription
    if (oldData.categoryName != newData.categoryName) map["categoryName"] = newData.categoryName
    if (oldData.imageUrl != newData.imageUrl) map["imageUrl"] = newData.imageUrl

    return map
}

fun compareRenterModel(
    oldData: Renter,
    newData: Renter
): HashMap<String, Any?> {

    val map: HashMap<String, Any?> = HashMap()

    if (oldData.timeStamp != newData.timeStamp) map["timeStamp"] = newData.timeStamp
    if (oldData.modified != newData.modified) map["modified"] = newData.modified
    if (oldData.name != newData.name) map["name"] = newData.name
    if (oldData.mobileNumber != newData.mobileNumber) map["mobileNumber"] = newData.mobileNumber
    if (oldData.emailId != newData.emailId) map["emailId"] = newData.emailId
    if (oldData.otherDocumentName != newData.otherDocumentName) map["otherDocumentName"] =
        newData.otherDocumentName
    if (oldData.otherDocumentNumber != newData.otherDocumentNumber) map["otherDocumentNumber"] =
        newData.otherDocumentNumber
    if (oldData.roomNumber != newData.roomNumber) map["roomNumber"] = newData.roomNumber
    if (oldData.address != newData.address) map["address"] = newData.address
    if (oldData.dueOrAdvanceAmount != newData.dueOrAdvanceAmount) map["dueOrAdvanceAmount"] =
        newData.dueOrAdvanceAmount
    if (oldData.renterId != newData.renterId) map["renterId"] = newData.renterId
    if (oldData.renterPassword != newData.renterPassword) map["renterPassword"] =
        newData.renterPassword
    if (oldData.isSupportingDocAdded != newData.isSupportingDocAdded) map["supportingDocAdded"] =
        newData.isSupportingDocAdded
    if (oldData.supportingDocument != newData.supportingDocument) map["supportingDocument"] =
        newData.supportingDocument
    if (oldData.status != newData.status) map["status"] = newData.status
    if (oldData.isSynced != newData.isSynced) map["synced"] = newData.isSynced

    return map
}