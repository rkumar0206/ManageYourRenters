package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BorrowerPaymentViewModel @Inject constructor(
    private val repository: BorrowerPaymentRepository
) : ViewModel() {

    fun insertBorrowerPayment(borrowerPayment: BorrowerPayment) = viewModelScope.launch {
        repository.insertBorrowerPayment(borrowerPayment)
    }

    fun insertBorrowerPayments(borrowerPayments: List<BorrowerPayment>) = viewModelScope.launch {
        repository.insertAllBorrowerPayment(borrowerPayments)
    }

    fun updateBorrowerPayment(borrowerPayment: BorrowerPayment) = viewModelScope.launch {
        repository.updateBorrowerPayment(borrowerPayment)
    }

    fun deleteBorrowerPayment(borrowerPayment: BorrowerPayment) = viewModelScope.launch {
        repository.deleteBorrowerPayment(borrowerPayment)
    }

    fun deleteAllBorrowerPayments() = viewModelScope.launch {
        repository.deleteAllBorrowerPayments()
    }

    fun deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey: String) = viewModelScope.launch {

        repository.deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey)
    }

    fun deleteBorrowerPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        repository.deleteBorrowerPaymentsByIsSynced(isSynced)
    }


    fun getAllBorrowerPayments() = repository.getAllBorrowerPayments().asLiveData()

    fun getPaymentsByBorrowerKey(borrowerKey: String) =
        repository.getPaymentsByBorrowerKey(borrowerKey).asLiveData()

    fun getTotalDueOfTheBorrower(borrowerKey: String) =
        repository.getTotalDueOfTheBorrower(borrowerKey).asLiveData()

    fun getBorrowerPaymentByKey(paymentKey: String) =
        repository.getBorrowerPaymentByKey(paymentKey).asLiveData()

    fun getPaymentKeysByBorrowerKey(borrowerKey: String) =
        repository.getPaymentKeysByBorrowerKey(borrowerKey).asLiveData()
}