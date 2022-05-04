package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentDateTimeInfo
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.monthlyPaymentServiceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyPaymentViewModel @Inject constructor(
    private val repository: MonthlyPaymentRepository
) : ViewModel() {

    fun insertMonthlyPayment(context: Context, monthlyPayment: MonthlyPayment) =
        viewModelScope.launch {

            if (Functions.isInternetAvailable(context)) {

                monthlyPaymentServiceHelper(
                    context,
                    monthlyPayment.key,
                    context.getString(R.string.post)
                )
                monthlyPayment.isSynced = true
            } else {

                monthlyPayment.isSynced = false
            }

            Functions.showToast(context, "Monthly payment saved")
            repository.insertMonthlyPayment(monthlyPayment)
        }

    fun insertAllMonthlyPayment(monthlyPayments: List<MonthlyPayment>) = viewModelScope.launch {
        repository.insertAllMonthlyPayment(monthlyPayments)
    }

    fun updateMonthlyPayment(context: Context, monthlyPayment: MonthlyPayment) =
        viewModelScope.launch {

            if (Functions.isInternetAvailable(context)) {

                monthlyPaymentServiceHelper(
                    context,
                    monthlyPayment.key,
                    context.getString(R.string.put)
                )
                monthlyPayment.isSynced = true
            } else {

                monthlyPayment.isSynced = false
            }

            repository.updateMonthlyPayment(monthlyPayment)
            Functions.showToast(context, "Payment updated")
        }

    fun deleteMonthlyPayment(context: Context, monthlyPayment: MonthlyPayment) =
        viewModelScope.launch {

            if (Functions.isInternetAvailable(context)) {

                monthlyPaymentServiceHelper(
                    context,
                    monthlyPayment.key,
                    context.getString(R.string.delete_one)
                )
            }

            repository.deleteMonthlyPayment(monthlyPayment)
            Functions.showToast(context, "Payment deleted")
        }

    fun deleteAllMonthlyPaymentByIsSynced(isSynced: Boolean) = viewModelScope.launch {
        repository.deleteAllMonthlyPaymentByIsSynced(isSynced)
    }

    fun deleteAllMonthlyPayments() = viewModelScope.launch {
        repository.deleteAllMonthlyPayments()
    }

    fun getAllMonthlyPayments() = repository.getAllMonthlyPayments().asLiveData()

    fun getAllMonthlyPaymentsByCategoryKey(categoryKey: String) =
        repository.getAllMonthlyPaymentsByCategoryKey(categoryKey).asLiveData()

    fun getMonthlyPaymentByKey(key: String) = repository.getMonthlyPaymentByKey(key).asLiveData()

    fun getLastMonthlyPayment(monthlyPaymentCategoryKey: String) =
        repository.getLastMonthlyPayment(monthlyPaymentCategoryKey).asLiveData()

    fun buildMonthlyPaymentInfoStringForAlertDialogMessage(
        monthlyPayment: MonthlyPayment,
        monthlyPaymentCategory: MonthlyPaymentCategory,
        monthList: List<String>
    ): String {

        val workingWithDateAndTime = WorkingWithDateAndTime()
        val message = StringBuilder()
        message.append(
            "\nModified On : ${
                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPayment.modified,
                    "dd-MM-yyyy hh:mm a"
                )
            }\n\n"
        )
        message.append(
            "Created On : ${
                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPayment.created,
                    "dd-MM-yyyy hh:mm a"
                )
            }\n\n---------------------------\n\n"
        )
        message.append(
            "Period : ${
                buildMonthlyPaymentPeriodString(
                    monthlyPayment.monthlyPaymentDateTimeInfo,
                    monthList
                )
            }\n\n"
        )
        if (monthlyPayment.monthlyPaymentDateTimeInfo?.paymentPeriodType == BillPeriodType.BY_MONTH) {
            message.append("Number of months : ${monthlyPayment.monthlyPaymentDateTimeInfo?.numberOfMonths}")
        } else {
            message.append("Number of days : ${monthlyPayment.monthlyPaymentDateTimeInfo?.numberOfDays}")
        }
        message.append("\n\n")
        message.append("Amount : ${monthlyPayment.amount}\n\n")
        if (monthlyPayment.message.isValid()) {
            message.append("Message : ${monthlyPayment.message}\n\n")
        }
        message.append("Category : ${monthlyPaymentCategory.categoryName}")
        return message.toString()
    }

    fun buildMonthlyPaymentPeriodString(
        monthlyPaymentDateTimeInfo: MonthlyPaymentDateTimeInfo?,
        monthList: List<String>
    ): String {

        val workingWithDateAndTime = WorkingWithDateAndTime()

        return if (monthlyPaymentDateTimeInfo?.paymentPeriodType == BillPeriodType.BY_MONTH) {
            val fromMonthYear =
                "${monthList[monthlyPaymentDateTimeInfo.forBillMonth - 1]}, " +
                        "${monthlyPaymentDateTimeInfo.forBillYear}"
            val toMonthYear =
                "${monthList[monthlyPaymentDateTimeInfo.toBillMonth - 1]}, " +
                        "${monthlyPaymentDateTimeInfo.toBillYear}"
            if (fromMonthYear == toMonthYear) {
                fromMonthYear
            } else {
                "$fromMonthYear to $toMonthYear"
            }
        } else {
            "${
                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPaymentDateTimeInfo?.fromBillDate
                )
            } to ${
                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPaymentDateTimeInfo?.toBillDate
                )
            }"
        }
    }
}