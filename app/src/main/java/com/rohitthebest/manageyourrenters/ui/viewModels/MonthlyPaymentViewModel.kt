package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentDateTimeInfo
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyPaymentViewModel @Inject constructor(
    app: Application,
    private val repository: MonthlyPaymentRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val MONTHLY_PAYMENT_RV_KEY = "fccdbshanajbjsbhve_d64fn"
    }

    fun saveMonthlyPaymentRvState(rvState: Parcelable?) {

        state.set(MONTHLY_PAYMENT_RV_KEY, rvState)
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

    fun insertAllMonthlyPayment(monthlyPayments: List<MonthlyPayment>) = viewModelScope.launch {
        repository.insertAllMonthlyPayment(monthlyPayments)
    }

    fun updateMonthlyPayment(oldValue: MonthlyPayment, newValue: MonthlyPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (Functions.isInternetAvailable(context)) {

                updateDocumentOnFireStore(
                    context,
                    compareMonthlyPaymentModel(oldValue, newValue),
                    MONTHLY_PAYMENTS,
                    oldValue.key
                )

                newValue.isSynced = true
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
            }

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