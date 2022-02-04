package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.repositories.EMIPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "EMIPaymentViewModel"

@HiltViewModel
class EMIPaymentViewModel @Inject constructor(
    private val emiPaymentRepository: EMIPaymentRepository,
    private val emiRepository: EMIRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val EMI_PAYMENT_RV_KEY = "sscjcbajbbcEMI_PAYMENT_RV_KEmzcnjdn"
    }

    fun saveEmiPaymentRvState(rvState: Parcelable?) {

        state.set(EMI_PAYMENT_RV_KEY, rvState)
    }

    private val _emiPaymentRvState: MutableLiveData<Parcelable> = state.getLiveData(
        EMI_PAYMENT_RV_KEY
    )

    val emiPaymentRvState: LiveData<Parcelable> get() = _emiPaymentRvState

    // ---------------------------------------------------------------


    fun insertEMIPayment(context: Context, emiPayment: EMIPayment) = viewModelScope.launch {

        emiPaymentRepository.insertEMIPayment(emiPayment)

        var isRefreshEnabled = true

        emiRepository.getEMIByKey(emiPayment.emiKey)
            .collect { emi ->

                if (isRefreshEnabled) {

                    emi.amountPaid += emiPayment.amountPaid
                    emi.monthsCompleted = emiPayment.tillMonth
                    emi.modified = System.currentTimeMillis()

                    updateEMI(context, emi)

                    isRefreshEnabled = false
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

    fun deleteEMIPayment(context: Context, emiPayment: EMIPayment) = viewModelScope.launch {

        if (isInternetAvailable(context)) {

            // delete supporting document
            if (emiPayment.isSupportingDocumentAdded) {

                if (emiPayment.supportingDocument != null
                    && emiPayment.supportingDocument?.documentType != DocumentType.URL
                ) {

                    deleteFileFromFirebaseStorage(
                        context,
                        emiPayment.supportingDocument?.documentUrl!!
                    )
                }
            }

            // delete the emi payment from firestore
            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.emiPayments),
                emiPayment.key
            )
        }

        emiPaymentRepository.deleteEMIPayment(emiPayment)

        var isRefreshEnabled = true

        emiRepository.getEMIByKey(emiPayment.emiKey).collect { emi ->

            if (isRefreshEnabled) {

                val amountPaid = emi.amountPaid - emiPayment.amountPaid
                val monthsCompleted = emiPayment.fromMonth - 1

                Log.d(TAG, "updateEMI: $amountPaid")
                Log.d(TAG, "updateEMI: $monthsCompleted")

                emi.amountPaid = amountPaid
                emi.monthsCompleted = monthsCompleted
                emi.modified = System.currentTimeMillis()

                updateEMI(context, emi)

                isRefreshEnabled = false

                return@collect
            }
        }

    }

    private suspend fun updateEMI(context: Context, emi: EMI) {

        if (isInternetAvailable(context)) {

            if (emi.isSynced) {

                val map = HashMap<String, Any?>()
                map["amountPaid"] = emi.amountPaid
                map["monthsCompleted"] = emi.monthsCompleted
                map["modified"] = emi.modified

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
                    context.getString(R.string.emis),
                    emi.key
                )
            }

        } else {

            emi.isSynced = false
        }

        emiRepository.updateEMI(emi)

    }

    fun deleteEMIPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        emiPaymentRepository.deleteEMIPaymentsByIsSynced(isSynced)
    }


/*
    fun deleteAllEMIPayments() = viewModelScope.launch {
        emiPaymentRepository.deleteAllEMIPayments()
    }

    fun deletePaymentsByEMIKey(emiKey: String) = viewModelScope.launch {

        emiPaymentRepository.deletePaymentsByEMIKey(emiKey)
    }
*/

    fun getAllEMIPaymentsByEMIKey(emiKey: String) =
        emiPaymentRepository.getAllEMIPaymentsByEMIKey(emiKey).asLiveData()


    /* fun getAllEMIPayments() = emiPaymentRepository.getAllEMIPayments().asLiveData()


     fun getEMIPaymentByKey(emiPaymentKey: String) =
         emiPaymentRepository.getEMIPaymentByKey(emiPaymentKey).asLiveData()
 */
}