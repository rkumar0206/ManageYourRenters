package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.repositories.PaymentMethodRepository
import com.rohitthebest.manageyourrenters.utils.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    app: Application,
    private val repository: PaymentMethodRepository
) : AndroidViewModel(app) {

    fun insertPaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (context.isInternetAvailable()) {

            paymentMethod.isSynced = true
            uploadDocumentToFireStore(
                context,
                FirestoreCollectionsConstants.PAYMENT_METHODS,
                paymentMethod.key
            )
        } else {
            paymentMethod.isSynced = false
        }

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