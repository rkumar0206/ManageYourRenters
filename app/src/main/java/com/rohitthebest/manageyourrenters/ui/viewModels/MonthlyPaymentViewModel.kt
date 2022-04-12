package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyPaymentViewModel @Inject constructor(
    private val repository: MonthlyPaymentRepository
) : ViewModel() {

    fun insertMonthlyPayment(monthlyPayment: MonthlyPayment) = viewModelScope.launch {
        repository.insertMonthlyPayment(monthlyPayment)
    }

    fun insertAllMonthlyPayment(monthlyPayments: List<MonthlyPayment>) = viewModelScope.launch {
        repository.insertAllMonthlyPayment(monthlyPayments)
    }

    fun updateMonthlyPayment(monthlyPayment: MonthlyPayment) = viewModelScope.launch {
        repository.updateMonthlyPayment(monthlyPayment)
    }

    fun deleteMonthlyPayment(monthlyPayment: MonthlyPayment) = viewModelScope.launch {
        repository.deleteMonthlyPayment(monthlyPayment)
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
}