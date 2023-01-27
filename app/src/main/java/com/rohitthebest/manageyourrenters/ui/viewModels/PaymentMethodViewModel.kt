package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.repositories.PaymentMethodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val repository: PaymentMethodRepository
) : ViewModel() {

    fun insertPaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch {
        repository.insertPaymentMethod(paymentMethod)
    }

    fun insertAllPaymentMethod(paymentMethods: List<PaymentMethod>) = viewModelScope.launch {
        repository.insertAllPaymentMethod(paymentMethods)
    }

    fun updatePaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch {
        repository.updatePaymentMethod(paymentMethod)
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch {
        repository.deletePaymentMethod(paymentMethod)
    }

    fun deleteAllPaymentMethods() = viewModelScope.launch {
        repository.deleteAllPaymentMethods()
    }

    fun getAllPaymentMethods() = repository.getAllPaymentMethods().asLiveData()

    fun getPaymentMethodByKey(paymentMethodKey: String) =
        repository.getPaymentMethodByKey(paymentMethodKey).asLiveData()
}