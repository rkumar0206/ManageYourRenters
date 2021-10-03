package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.repositories.EMIPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.utils.fromEMIToString
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EMIPaymentViewModel @Inject constructor(
    private val emiPaymentRepository: EMIPaymentRepository,
    private val emiRepository: EMIRepository
) : ViewModel() {

    fun insertEMIPayment(context: Context, emiPayment: EMIPayment) = viewModelScope.launch {
        emiPaymentRepository.insertEMIPayment(emiPayment)

        emiRepository.getEMIByKey(emiPayment.emiKey)
            .collect { emi ->

                emiPaymentRepository.getTotalAmountPaidOfAnEMI(emi.key)
                    .collect { value ->

                        emi.amountPaid = value
                        emi.monthsCompleted = emiPayment.tillMonth
                        emi.modified = System.currentTimeMillis()

                        val map = HashMap<String, Any?>()
                        map["amountPaid"] = value
                        map["monthsCompleted"] = emiPayment.tillMonth
                        map["modified"] = System.currentTimeMillis()


                        if (emi.isSynced) {

                            updateDocumentOnFireStore(
                                context,
                                map,
                                context.getString(R.string.emis),
                                emi.key
                            )
                        } else {

                            emi.isSynced = true

                            uploadDocumentToFireStore(
                                context,
                                fromEMIToString(emi),
                                context.getString(R.string.emis),
                                emi.key
                            )
                        }

                        emiRepository.updateEMI(emi)
                    }

                return@collect
            }

    }

    fun insertAllEMIPayment(emiPayments: List<EMIPayment>) = viewModelScope.launch {
        emiPaymentRepository.insertAllEMIPayment(emiPayments)
    }

    fun updateEMIPayment(emiPayment: EMIPayment) = viewModelScope.launch {
        emiPaymentRepository.updateEMIPayment(emiPayment)
    }

    fun deleteEMIPayment(emiPayment: EMIPayment) = viewModelScope.launch {
        emiPaymentRepository.deleteEMIPayment(emiPayment)
    }

    fun deleteAllEMIPayments() = viewModelScope.launch {
        emiPaymentRepository.deleteAllEMIPayments()
    }

    fun deletePaymentsByEMIKey(emiKey: String) = viewModelScope.launch {

        emiPaymentRepository.deletePaymentsByEMIKey(emiKey)
    }

    fun deleteEMIPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        emiPaymentRepository.deleteEMIPaymentsByIsSynced(isSynced)
    }

    fun getAllEMIPayments() = emiPaymentRepository.getAllEMIPayments().asLiveData()

    fun getAllEMIPaymentsByEMIKey(emiKey: String) =
        emiPaymentRepository.getAllEMIPaymentsByEMIKey(emiKey).asLiveData()

    fun getEMIPaymentByKey(emiPaymentKey: String) =
        emiPaymentRepository.getEMIPaymentByKey(emiPaymentKey).asLiveData()

}