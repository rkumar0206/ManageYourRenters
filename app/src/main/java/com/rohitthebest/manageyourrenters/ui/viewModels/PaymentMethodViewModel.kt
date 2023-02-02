package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.repositories.PaymentMethodRepository
import com.rohitthebest.manageyourrenters.utils.*
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
        uploadToFireStore(context, paymentMethod)
        repository.insertPaymentMethod(paymentMethod)
    }

    fun syncPaymentWithCloud(paymentMethod: PaymentMethod) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext
        uploadToFireStore(context, paymentMethod)
        repository.updatePaymentMethod(paymentMethod)
    }

    private fun uploadToFireStore(context: Context, paymentMethod: PaymentMethod) {

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
    }

    fun insertAllPaymentMethod(paymentMethods: List<PaymentMethod>) = viewModelScope.launch {
        repository.insertAllPaymentMethod(paymentMethods)
    }

    fun updatePaymentMethod(oldValue: PaymentMethod, newValue: PaymentMethod) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (Functions.isInternetAvailable(context)) {

                val map = comparePaymentMethod(oldValue, newValue)
                if (map.isNotEmpty()) {
                    updateDocumentOnFireStore(
                        context,
                        map,
                        FirestoreCollectionsConstants.PAYMENT_METHODS,
                        oldValue.key
                    )
                }
            } else {
                newValue.isSynced = false
            }

            repository.updatePaymentMethod(newValue)
        }

    fun deletePaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext
        deleteDocumentFromFireStore(
            context,
            FirestoreCollectionsConstants.PAYMENT_METHODS,
            paymentMethod.key
        )
        repository.deletePaymentMethod(paymentMethod)
    }

    fun deleteAllPaymentMethods() = viewModelScope.launch {
        repository.deleteAllPaymentMethods()
    }

    fun getAllPaymentMethods() = repository.getAllPaymentMethods().asLiveData()

    fun getPaymentMethodByKey(paymentMethodKey: String) =
        repository.getPaymentMethodByKey(paymentMethodKey).asLiveData()
}