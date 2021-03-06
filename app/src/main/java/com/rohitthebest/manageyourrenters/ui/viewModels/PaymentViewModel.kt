package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.repositories.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    val repository: PaymentRepository
) : ViewModel() {

    fun insertPayment(payment: Payment) = viewModelScope.launch {

        repository.insertPayment(payment)
    }

    fun insertPayments(payments: List<Payment>) = viewModelScope.launch {

        repository.insertPayments(payments)
    }

    fun deletePayment(payment: Payment) = viewModelScope.launch {

        repository.deletePayment(payment)
    }

    fun deleteAllPayments() = viewModelScope.launch {

        repository.deleteAllPayments()
    }

    fun deleteAllPaymentsOfRenter(renterKey: String) = viewModelScope.launch {

        repository.deleteAllPaymentsOfRenter(renterKey)
    }

    fun deleteAllPaymentsByIsSynced(isSynced: String) = viewModelScope.launch {

        repository.deleteAllPaymentsByIsSynced(isSynced)
    }

    fun getAllPaymentsList() = repository.getAllPaymentsList()

    fun getAllPaymentsListOfRenter(renterKey: String) =
        repository.getAllPaymentsListOfRenter(renterKey)

    fun getCountOfPaymentsOfRenter(renterKey: String) =
        repository.getCountOfPaymentsOfRenter(renterKey)

    fun getSumOfDueOrAdvance(renterKey: String) = repository.getSumOfDueOrAdvance(renterKey)
}