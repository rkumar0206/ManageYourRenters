package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.repositories.EMIPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "EMIPaymentViewModel"

@HiltViewModel
class EMIPaymentViewModel @Inject constructor(
    app: Application,
    private val emiPaymentRepository: EMIPaymentRepository,
    private val emiRepository: EMIRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

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


    fun insertEMIPayment(
        emiPayment: EMIPayment,
        supportingDocumentHelperModel: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            emiPayment.isSynced = true
            uploadDocumentToFireStore(
                context,
                context.getString(R.string.emiPayments),
                emiPayment.key
            )

            if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL) {

                supportingDocumentHelperModel.modelName =
                    context.getString(R.string.emiPayments)

                uploadFileToFirebaseCloudStorage(
                    context,
                    supportingDocumentHelperModel,
                    emiPayment.key
                )
            }

        } else {

            emiPayment.isSynced = false
        }

        emiPaymentRepository.insertEMIPayment(emiPayment)

        updateEMI(emiPayment, true)
    }

    fun insertAllEMIPayment(emiPayments: List<EMIPayment>) = viewModelScope.launch {
        emiPaymentRepository.insertAllEMIPayment(emiPayments)
    }

    fun updateEMIPayment(emiPayment: EMIPayment) = viewModelScope.launch {
        emiPaymentRepository.updateEMIPayment(emiPayment)
    }

    fun deleteEMIPayment(emiPayment: EMIPayment) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            // delete supporting document
            if (emiPayment.isSupportingDocAdded
                && emiPayment.supportingDocument != null
                && emiPayment.supportingDocument?.documentType != DocumentType.URL
            ) {

                deleteFileFromFirebaseStorage(
                    context,
                    emiPayment.supportingDocument?.documentUrl!!
                )
            }

            // delete the emi payment from firestore
            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.emiPayments),
                emiPayment.key
            )
        }

        updateEMI(emiPayment.copy(), false)
        emiPaymentRepository.deleteEMIPayment(emiPayment)
    }

    private suspend fun updateEMI(emiPayment: EMIPayment, isInsert: Boolean = true) {

        val context = getApplication<Application>().applicationContext

        val emi = emiRepository.getEMIByKey(emiPayment.emiKey).first()

        val oldEMI = emi.copy()

        if (!isInsert) {

            emi.amountPaid = emi.amountPaid - emiPayment.amountPaid
            emi.monthsCompleted = emiPayment.fromMonth - 1
        } else {
            emi.amountPaid += emiPayment.amountPaid
            emi.monthsCompleted = emiPayment.tillMonth
        }
        emi.modified = System.currentTimeMillis()

        if (isInternetAvailable(context)) {

            if (!oldEMI.isSynced) {

                emi.isSynced = true
                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.emis),
                    emi.key
                )
            } else {

                val map = compareEmi(oldEMI, emi)
                updateDocumentOnFireStore(
                    context,
                    map,
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

    fun getLastEMIPaymentOfEMIbyEMIKey(emiKey: String) =
        emiPaymentRepository.getLastEMIPaymentOfEMIbyEMIKey(emiKey).asLiveData()

    /* fun getAllEMIPayments() = emiPaymentRepository.getAllEMIPayments().asLiveData()


     fun getEMIPaymentByKey(emiPaymentKey: String) =
         emiPaymentRepository.getEMIPaymentByKey(emiPaymentKey).asLiveData()
 */
}