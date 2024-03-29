package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMI_PAYMENTS
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
                EMI_PAYMENTS,
                emiPayment.key
            )

            if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL) {

                supportingDocumentHelperModel.modelName =
                    EMI_PAYMENTS

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

    fun updateEMIPayment(oldEmiPayment: EMIPayment, emiPayment: EMIPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                emiPayment.isSynced = true

                if (!oldEmiPayment.isSynced) {

                    uploadDocumentToFireStore(
                        context,
                        EMI_PAYMENTS,
                        emiPayment.key
                    )
                } else {

                    val map = compareEMIPaymentModel(oldEmiPayment, emiPayment)

                    if (map.isNotEmpty()) {

                        updateDocumentOnFireStore(
                            context,
                            map,
                            EMI_PAYMENTS,
                            emiPayment.key
                        )
                    }
                }
            } else {
                emiPayment.isSynced = false
            }

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
                EMI_PAYMENTS,
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

    fun addOrReplaceBorrowerSupportingDocument(
        emiPayment: EMIPayment,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

        val oldEMIPayment = emiPayment.copy()

        emiPayment.modified = System.currentTimeMillis()

        if (emiPayment.supportingDocument != null && emiPayment.supportingDocument?.documentType != DocumentType.URL) {

            deleteFileFromFirebaseStorage(
                context,
                emiPayment.supportingDocument?.documentUrl!!
            )
        }

        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            emiPayment.isSupportingDocAdded = true
            emiPayment.supportingDocument = supportingDoc

            updateEMIPayment(oldEMIPayment, emiPayment)
        } else {

            supportDocumentHelper.modelName = EMI_PAYMENTS

            if (!emiPayment.isSynced) {
                insertEMIPayment(emiPayment, supportDocumentHelper)
                return
            }
            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, emiPayment.key
            )
        }
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

    fun buildEMIPaymentInfoStringForAlertDialogMessage(
        emiPayment: EMIPayment,
        emi: EMI
    ): String {

        val message = StringBuilder()
        message.append(
            "\nModified On : ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    emiPayment.modified,
                    "dd-MM-yyyy hh:mm a"
                )
            }\n\n"
        )
        message.append(
            "Created On : ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    emiPayment.created,
                    "dd-MM-yyyy hh:mm a"
                )
            }\n\n---------------------------\n\n"
        )

        if (emiPayment.fromMonth == emiPayment.tillMonth) {

            message.append("For month : ${emiPayment.fromMonth}\n\n")
        } else {
            message.append("From month : ${emiPayment.fromMonth}\nTill month : ${emiPayment.tillMonth}\n\n")
        }

        message.append("Amount paid : ${emi.currencySymbol} ${emiPayment.amountPaid}\n\n")

        if (emiPayment.message.isValid()) {

            message.append("Message : ${emiPayment.message}\n\n")
        }

        message.append("For EMI : ${emi.emiName}")
        return message.toString()
    }

    /* fun getAllEMIPayments() = emiPaymentRepository.getAllEMIPayments().asLiveData()


     fun getEMIPaymentByKey(emiPaymentKey: String) =
         emiPaymentRepository.getEMIPaymentByKey(emiPaymentKey).asLiveData()
 */
}