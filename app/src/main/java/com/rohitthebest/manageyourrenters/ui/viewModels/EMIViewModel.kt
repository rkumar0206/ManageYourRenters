package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.repositories.EMIPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "EMIViewModel"

@HiltViewModel
class EMIViewModel @Inject constructor(
    app: Application,
    private val emiRepository: EMIRepository,
    private val emiPaymentRepository: EMIPaymentRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val EMI_RV_KEY = "jcabbbcEMI_RV_KEYabjsnjdn"
    }

    fun saveEmiRvState(rvState: Parcelable?) {

        state.set(EMI_RV_KEY, rvState)
    }

    private val _emiRvState: MutableLiveData<Parcelable> = state.getLiveData(
        EMI_RV_KEY
    )

    val emiRvState: LiveData<Parcelable> get() = _emiRvState

    // ---------------------------------------------------------------

    fun insertEMI(
        emi: EMI,
        supportingDocumentHelperModel: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            emi.isSynced = true

            uploadDocumentToFireStore(
                context,
                context.getString(R.string.emis),
                emi.key
            )

            if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL
            ) {
                supportingDocumentHelperModel.modelName =
                    context.getString(R.string.emis)
                uploadFileToFirebaseCloudStorage(
                    context, supportingDocumentHelperModel, emi.key
                )
            }

        } else {

            emi.isSynced = false
        }

        emiRepository.insertEMI(emi)
    }

    fun insertAllEMI(emis: List<EMI>) = viewModelScope.launch {
        emiRepository.insertAllEMI(emis)
    }

    fun updateEMI(oldEMI: EMI, emi: EMI) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            emi.isSynced = true

            if (!oldEMI.isSynced) {

                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.emis),
                    emi.key
                )
            } else {

                val map = compareEmi(oldEMI, emi)

                if (map.isNotEmpty()) {
                    updateDocumentOnFireStore(
                        context,
                        map,
                        context.getString(R.string.emis),
                        emi.key
                    )
                }
            }
        } else {
            emi.isSynced = false
        }

        emiRepository.updateEMI(emi)
    }

    fun deleteEMI(emi: EMI) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        // get all the supporting documents and keys of payments for this emi
        val keysAndSupportingDocs =
            emiPaymentRepository.getEmiPaymentsKeysAndSupportingDocsByEMIKey(
                emi.key
            )

        Log.d(TAG, "deleteEMI: $keysAndSupportingDocs")

        if (keysAndSupportingDocs.isNotEmpty()) {

            // extract the keys and supporting doc urls from the list
            val keys = keysAndSupportingDocs.map { it.key }
            val supportingDocs = keysAndSupportingDocs.map { it.supportingDocument }
                .filter { it != null && it.documentType != DocumentType.URL }

            // delete the supporting document from the firebase storage
            if (supportingDocs.isNotEmpty()) {

                supportingDocs.forEach { supportingDoc ->

                    supportingDoc?.let { deleteFileFromFirebaseStorage(context, it.documentUrl) }
                }
            }

            // delete all emi payments from firestore
            deleteAllDocumentsUsingKeyFromFirestore(
                context,
                context.getString(R.string.emiPayments),
                convertStringListToJSON(keys)
            )

        }

        // delete supporting document of the emi
        if (emi.isSupportingDocAdded) {

            if (emi.supportingDocument != null && emi.supportingDocument?.documentType != DocumentType.URL)

                deleteFileFromFirebaseStorage(context, emi.supportingDocument?.documentUrl!!)
        }

        // delete the emi from the firestore
        deleteDocumentFromFireStore(
            context,
            context.getString(R.string.emis),
            emi.key
        )

        // delete from local database
        emiPaymentRepository.deletePaymentsByEMIKey(emi.key)
        emiRepository.deleteEMI(emi)
    }

    fun deleteAllEMIs() = viewModelScope.launch {
        emiRepository.deleteAllEMIs()
        emiPaymentRepository.deleteAllEMIPayments()
    }

    fun deleteEMIsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        emiRepository.deleteEMIsByIsSynced(isSynced)
    }

    fun getAllEMIs() = emiRepository.getAllEMIs().asLiveData()

    fun getEMIByKey(emiKey: String) = emiRepository.getEMIByKey(emiKey).asLiveData()

}