package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.RenterPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RenterPaymentViewModel @Inject constructor(
    app: Application,
    private val paymentRepository: RenterPaymentRepository,
    private val renterRepository: RenterRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val RENTER_PAYMENT_RV_KEY = "jbkjbajacjhbgaaagyqvgvdqv"
    }

    fun saveRenterPaymentRvState(rvState: Parcelable?) {

        state.set(RENTER_PAYMENT_RV_KEY, rvState)
    }

    private val _renterPaymentRvState: MutableLiveData<Parcelable> = state.getLiveData(
        RENTER_PAYMENT_RV_KEY
    )

    val renterPaymentRvState: LiveData<Parcelable> get() = _renterPaymentRvState

    // ---------------------------------------------------------------

    fun insertPayment(
        renterPayment: RenterPayment,
        supportingDocumentHelperModel: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        updateRenterDuesOrAdvance(renterPayment, renterPayment.renterKey)

        if (isInternetAvailable(context)) {

            renterPayment.isSynced = true
            uploadDocumentToFireStore(
                context,
                RENTER_PAYMENTS,
                renterPayment.key
            )

            if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL) {

                supportingDocumentHelperModel.modelName =
                    RENTER_PAYMENTS

                uploadFileToFirebaseCloudStorage(
                    context,
                    supportingDocumentHelperModel,
                    renterPayment.key
                )
            }

        } else {

            renterPayment.isSynced = false
        }

        paymentRepository.insertRenterPayment(renterPayment)
    }

    private fun updateRenterDuesOrAdvance(
        renterPayment: RenterPayment?,
        renterKey: String
    ) =

        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            val renter: Renter = renterRepository.getRenterByKey(renterKey).first()

            var amountPaid = 0.0
            var netDemand = 0.0

            if (renterPayment != null) {

                amountPaid = renterPayment.amountPaid
                netDemand = renterPayment.netDemand
            }

            renter.modified = System.currentTimeMillis()
            renter.dueOrAdvanceAmount = amountPaid - netDemand

            if (isInternetAvailable(context)) {

                if (renter.isSynced == context.getString(R.string.f)) {

                    // upload to firestore
                    renter.isSynced = context.getString(R.string.t)

                    uploadDocumentToFireStore(
                        context,
                        context.getString(R.string.renters),
                        renter.key!!
                    )

                } else {

                    // update on firestore

                    val map = HashMap<String, Any?>()

                    map["modified"] = renter.modified
                    map["dueOrAdvanceAmount"] = renter.dueOrAdvanceAmount

                    updateDocumentOnFireStore(
                        context,
                        map,
                        context.getString(R.string.renters),
                        renter.key!!
                    )

                }
            } else {

                renter.isSynced = context.getString(R.string.f)
            }

            renterRepository.updateRenter(renter)
        }


    fun updatePayment(
        oldRenterPayment: RenterPayment,
        renterPayment: RenterPayment
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            renterPayment.isSynced = true

            if (!oldRenterPayment.isSynced) {

                uploadDocumentToFireStore(
                    context,
                    RENTER_PAYMENTS,
                    renterPayment.key
                )
            } else {

                val map = compareRenterPaymentModel(oldRenterPayment, renterPayment)
                if (map.isNotEmpty()) {
                    updateDocumentOnFireStore(
                        context,
                        map,
                        RENTER_PAYMENTS,
                        renterPayment.key
                    )
                }
            }
        } else {

            renterPayment.isSynced = false
        }

        paymentRepository.updateRenterPayment(renterPayment)
    }

    fun insertPayments(renterPayments: List<RenterPayment>) = viewModelScope.launch {

        paymentRepository.insertAllRenterPayment(renterPayments)
    }

    fun deletePayment(renterPayment: RenterPayment) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        val renterKey = renterPayment.renterKey

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                RENTER_PAYMENTS,
                renterPayment.key
            )

            if (renterPayment.isSupportingDocAdded
                && renterPayment.supportingDocument != null
                && renterPayment.supportingDocument?.documentType != DocumentType.URL
            ) {

                deleteFileFromFirebaseStorage(
                    context,
                    renterPayment.supportingDocument?.documentUrl!!
                )
            }

        }

        paymentRepository.deleteRenterPayment(renterPayment)

        // update renter's due or advance from last payment after deleting this payment
        val lastPayment = paymentRepository.getLastRenterPayment(renterKey).first()

        updateRenterDuesOrAdvance(lastPayment, renterKey)
    }

    fun addOrReplaceBorrowerSupportingDocument(
        renterPayment: RenterPayment,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

        val oldRenterPayment = renterPayment.copy()

        if (oldRenterPayment.supportingDocument != null && oldRenterPayment.supportingDocument?.documentType != DocumentType.URL) {

            // if borrower payment contains supporting document previously, then call delete service also

            deleteFileFromFirebaseStorage(
                context,
                renterPayment.supportingDocument?.documentUrl!!
            )
        }

        renterPayment.modified = System.currentTimeMillis()
        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            renterPayment.isSupportingDocAdded = true
            renterPayment.supportingDocument = supportingDoc

            updatePayment(oldRenterPayment, renterPayment)
        } else {

            supportDocumentHelper.modelName = RENTER_PAYMENTS

            if (!oldRenterPayment.isSynced) {
                insertPayment(renterPayment, supportDocumentHelper)
                return
            }
            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, renterPayment.key
            )
        }
    }


    fun deleteAllPaymentsOfRenter(renterKey: String) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            val keysAndSupportingDocs =
                paymentRepository.getPaymentKeysAndSupportingDocumentByRenterKey(renterKey)

            val keys = keysAndSupportingDocs.map { it.key }
            val supportingDocument = keysAndSupportingDocs.map { it.supportingDocument }
                .filter { it != null && it.documentType != DocumentType.URL }

            supportingDocument.forEach { supportingDoc ->

                supportingDoc?.let { deleteFileFromFirebaseStorage(context, it.documentUrl) }
            }

            if (keysAndSupportingDocs.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    RENTER_PAYMENTS,
                    convertStringListToJSON(keys)
                )
            }
        }
        paymentRepository.deleteAllPaymentsOfRenter(renterKey)
    }

    fun deleteAllRenterPayments() = viewModelScope.launch {

        paymentRepository.deleteAllRenterPayments()
    }

    fun deleteAllPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        paymentRepository.deleteAllPaymentsByIsSynced(isSynced)
    }

    fun getAllPaymentsListOfRenter(renterKey: String) =
        paymentRepository.getAllPaymentsListOfRenter(renterKey).asLiveData()

    fun getPaymentByPaymentKey(paymentKey: String) =
        paymentRepository.getPaymentByPaymentKey(paymentKey).asLiveData()

    fun getLastRenterPayment(renterKey: String) =
        paymentRepository.getLastRenterPayment(renterKey).asLiveData()

    fun getAllRenterPayments() = paymentRepository.getAllRenterPayments().asLiveData()

    fun getTotalRevenueOfAllTime() = paymentRepository.getTotalRevenueOfAllTime().asLiveData()
}