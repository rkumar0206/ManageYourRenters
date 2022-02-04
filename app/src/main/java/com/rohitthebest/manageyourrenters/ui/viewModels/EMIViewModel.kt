package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.repositories.EMIPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.deleteAllDocumentsUsingKeyFromFirestore
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "EMIViewModel"

@HiltViewModel
class EMIViewModel @Inject constructor(
    private val emiRepository: EMIRepository,
    private val emiPaymentRepository: EMIPaymentRepository,
    private val state: SavedStateHandle
) : ViewModel() {

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

    fun insertEMI(emi: EMI) = viewModelScope.launch {
        emiRepository.insertEMI(emi)
    }

    fun insertAllEMI(emis: List<EMI>) = viewModelScope.launch {
        emiRepository.insertAllEMI(emis)
    }

    fun updateEMI(emi: EMI) = viewModelScope.launch {
        emiRepository.updateEMI(emi)
    }

    fun deleteEMI(context: Context, emi: EMI) = viewModelScope.launch {

        // get all the supporting documents and keys of payments for this emi
        val keysAndSupportingDocs =
            emiPaymentRepository.getEmiPaymentsKeysAndSupportingDocsByEMIKey(
                emi.key
            )

        Log.d(TAG, "deleteEMI: $keysAndSupportingDocs")

        if (keysAndSupportingDocs.isNotEmpty()) {

            // extract the keys and supporting doc urls from the list
            val keys: ArrayList<String> = ArrayList()
            val supportingDocs: ArrayList<SupportingDocument?> = ArrayList()

            keysAndSupportingDocs.forEach { keyAndSupportingDoc ->

                keys.add(keyAndSupportingDoc.key)
                supportingDocs.add(keyAndSupportingDoc.supportingDocument)
            }

            Log.d(TAG, "deleteEMI: Keys : $keys")
            Log.d(TAG, "deleteEMI: Supporting doc : $supportingDocs")

            // delete the supporting document from the firebase storage
            if (supportingDocs.isNotEmpty()) {

                supportingDocs.forEach { supportingDoc ->

                    if (supportingDoc != null && supportingDoc.documentType != DocumentType.URL) {

                        deleteFileFromFirebaseStorage(context, supportingDoc.documentUrl)
                    }
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
        if (emi.isSupportingDocumentAdded) {

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