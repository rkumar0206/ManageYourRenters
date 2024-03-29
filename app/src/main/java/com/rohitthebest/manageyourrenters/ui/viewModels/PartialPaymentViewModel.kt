package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartialPaymentViewModel @Inject constructor(
    private val repository: PartialPaymentRepository
) : ViewModel() {

    fun insertPartialPayment(partialPayment: PartialPayment) = viewModelScope.launch {
        repository.insertPartialPayment(partialPayment)
    }

    fun insertAllPartialPayment(partialPayments: List<PartialPayment>) = viewModelScope.launch {
        repository.insertAllPartialPayment(partialPayments)
    }

    fun deletePartialPayment(partialPayment: PartialPayment) = viewModelScope.launch {
        repository.deletePartialPayment(partialPayment)
    }

    fun deleteAllPartialPayments() = viewModelScope.launch {
        repository.deleteAllPartialPayments()
    }

    fun deletePartialPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        repository.deletePartialPaymentsByIsSynced(isSynced)
    }

    fun deleteAllByProvideList(partialPaymentKeys: List<String>) =
        viewModelScope.launch {

            repository.deleteAllByProvideList(partialPaymentKeys)
        }

    fun getAllPartialPayments() = repository.getAllPartialPayments().asLiveData()

    fun getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String) =
        repository.getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey).asLiveData()

}