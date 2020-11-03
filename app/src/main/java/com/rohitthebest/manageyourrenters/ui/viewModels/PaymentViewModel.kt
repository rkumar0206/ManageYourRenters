package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.repositories.PaymentRepository
import kotlinx.coroutines.launch

class PaymentViewModel @ViewModelInject constructor(
    val repository: PaymentRepository
) : ViewModel() {

    fun insertPayment(payment: Payment) = viewModelScope.launch {

        repository.insertPayment(payment)
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

    fun getAllPaymentsList() = repository.getAllPaymentsList()

    fun getAllPaymentsListOfRenter(renterKey: String) =
        repository.getAllPaymentsListOfRenter(renterKey)

    fun getCountOfPaymentsOfRenter(renterKey: String) =
        repository.getCountOfPaymentsOfRenter(renterKey)
}