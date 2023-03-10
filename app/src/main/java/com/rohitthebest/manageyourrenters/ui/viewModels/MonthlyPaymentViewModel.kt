package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentDateTimeInfo
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyPaymentViewModel @Inject constructor(
    private val repository: MonthlyPaymentRepository,
    private val expenseRepository: ExpenseRepository,
    app: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val MONTHLY_PAYMENT_RV_KEY = "fccdbshanajbjsbhve_d64fn"
    }

    fun saveMonthlyPaymentRvState(rvState: Parcelable?) {

        state[MONTHLY_PAYMENT_RV_KEY] = rvState
    }

    private val _monthlyPaymentRvState: MutableLiveData<Parcelable> = state.getLiveData(
        MONTHLY_PAYMENT_RV_KEY
    )

    val monthlyPaymentRvState: LiveData<Parcelable> get() = _monthlyPaymentRvState

    // ---------------------------------------------------------------


    fun insertMonthlyPayment(monthlyPayment: MonthlyPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (Functions.isInternetAvailable(context)) {

                monthlyPayment.isSynced = true

                uploadDocumentToFireStore(
                    context,
                    MONTHLY_PAYMENTS,
                    monthlyPayment.key
                )

            } else {

                monthlyPayment.isSynced = false
            }

            Functions.showToast(context, "Monthly payment saved")
            repository.insertMonthlyPayment(monthlyPayment)
        }


    fun updateMonthlyPayment(oldValue: MonthlyPayment, newValue: MonthlyPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (Functions.isInternetAvailable(context)) {

                newValue.isSynced = true

                if (!oldValue.isSynced) {

                    uploadDocumentToFireStore(context, MONTHLY_PAYMENTS, newValue.key)
                } else {

                    val map = compareMonthlyPaymentModel(oldValue, newValue)
                    if (map.isNotEmpty()) {
                        updateDocumentOnFireStore(
                            context,
                            map,
                            MONTHLY_PAYMENTS,
                            oldValue.key
                        )
                    }
                }
            } else {

                newValue.isSynced = false
            }

            repository.updateMonthlyPayment(newValue)
            Functions.showToast(context, "Payment updated")
        }

    fun deleteMonthlyPayment(monthlyPayment: MonthlyPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (Functions.isInternetAvailable(context)) {

                deleteDocumentFromFireStore(
                    context,
                    MONTHLY_PAYMENTS,
                    monthlyPayment.key
                )

                // issue #12
                delay(50)
                deleteDocumentFromFireStore(
                    context,
                    EXPENSES,
                    monthlyPayment.key
                )
            }

            // issue #12
            expenseRepository.deleteExpenseByKey(monthlyPayment.key)

            repository.deleteMonthlyPayment(monthlyPayment)
            Functions.showToast(context, "Payment deleted")
        }

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

        val message = StringBuilder()
        message.append(
            "\nModified On : ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPayment.modified,
                    "dd-MM-yyyy hh:mm a"
                )
            }\n\n"
        )
        message.append(
            "Created On : ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPaymentDateTimeInfo?.fromBillDate
                )
            } to ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    monthlyPaymentDateTimeInfo?.toBillDate
                )
            }"
        }
    }
}