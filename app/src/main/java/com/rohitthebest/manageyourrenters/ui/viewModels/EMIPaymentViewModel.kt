package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.repositories.EMIPaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EMIPaymentViewModel @Inject constructor(
    private val repository: EMIPaymentRepository
) : ViewModel() {

    fun insertEMIPayment(context: Context, emiPayment: EMIPayment) = viewModelScope.launch {
        repository.insertEMIPayment(emiPayment)

        // todo : update the total month completed and amount paid of EMI

    }

    fun insertAllEMIPayment(emiPayments: List<EMIPayment>) = viewModelScope.launch {
        repository.insertAllEMIPayment(emiPayments)
    }

    fun updateEMIPayment(emiPayment: EMIPayment) = viewModelScope.launch {
        repository.updateEMIPayment(emiPayment)
    }

    fun deleteEMIPayment(emiPayment: EMIPayment) = viewModelScope.launch {
        repository.deleteEMIPayment(emiPayment)
    }

    fun deleteAllEMIPayments() = viewModelScope.launch {
        repository.deleteAllEMIPayments()
    }

    fun deletePaymentsByEMIKey(emiKey: String) = viewModelScope.launch {

        repository.deletePaymentsByEMIKey(emiKey)
    }

    fun deleteEMIPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        repository.deleteEMIPaymentsByIsSynced(isSynced)
    }

    fun getAllEMIPayments() = repository.getAllEMIPayments().asLiveData()

    fun getAllEMIPaymentsByEMIKey(emiKey: String) =
        repository.getAllEMIPaymentsByEMIKey(emiKey).asLiveData()

    fun getEMIPaymentByKey(emiPaymentKey: String) =
        repository.getEMIPaymentByKey(emiPaymentKey).asLiveData()

}