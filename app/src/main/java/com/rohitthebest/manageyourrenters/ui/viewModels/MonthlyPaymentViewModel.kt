package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.expenseServiceHelper
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

                expenseServiceHelper(
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
}