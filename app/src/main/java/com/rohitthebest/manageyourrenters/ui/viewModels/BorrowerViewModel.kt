package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.InterestCalculatorFields
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.PARTIAL_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateInterestAndAmount
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateNumberOfDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerViewModel"

@HiltViewModel
class BorrowerViewModel @Inject constructor(
    app: Application,
    private val borrowerRepository: BorrowerRepository,
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val BORROWER_RV_KEY = "adfnjssaaafaaba_dcnjdn"
    }

    fun saveBorrowerRvState(rvState: Parcelable?) {

        state.set(BORROWER_RV_KEY, rvState)
    }

    private val _borrowerRvState: MutableLiveData<Parcelable> = state.getLiveData(
        BORROWER_RV_KEY
    )

    val borrowerRvState: LiveData<Parcelable> get() = _borrowerRvState

    // ---------------------------------------------------------------


    fun insertBorrower(
        borrower: Borrower,
        supportDocumentHelper: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            borrower.isSynced = true

            uploadDocumentToFireStore(
                context,
                BORROWERS,
                borrower.key
            )

            if (supportDocumentHelper != null
                && supportDocumentHelper.documentType != DocumentType.URL
            ) {

                supportDocumentHelper.modelName = BORROWERS
                uploadFileToFirebaseCloudStorage(
                    context, supportDocumentHelper, borrower.key
                )
            }

        } else {

            borrower.isSynced = false
        }

        borrowerRepository.insertBorrower(borrower)
    }

    fun insertBorrowers(borrowers: List<Borrower>) = viewModelScope.launch {

        borrowerRepository.insertBorrowers(borrowers)
    }

    fun updateBorrower(borrower: Borrower) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            borrower.isSynced = true

            uploadDocumentToFireStore(
                context,
                BORROWERS,
                borrower.key
            )
        } else {

            borrower.isSynced = false
        }

        borrowerRepository.update(borrower)
        getAllBorrower()
    }

    fun addOrReplaceBorrowerSupportingDocument(
        borrower: Borrower,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

        borrower.modified = System.currentTimeMillis()

        if (borrower.supportingDocument != null && borrower.supportingDocument?.documentType != DocumentType.URL) {

            // if borrower contains supporting document previously, then call delete service also

            deleteFileFromFirebaseStorage(
                context,
                borrower.supportingDocument?.documentUrl!!
            )
        }

        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            borrower.isSupportingDocAdded = true
            borrower.supportingDocument = supportingDoc

            updateBorrower(borrower)
        } else {

            supportDocumentHelper.modelName = BORROWERS

            if (!borrower.isSynced) {
                insertBorrower(borrower, supportDocumentHelper)
                return
            }
            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, borrower.key
            )
        }
    }

    fun deleteBorrower(borrower: Borrower) = viewModelScope.launch {
        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                BORROWERS,
                borrower.key
            )

            if (borrower.supportingDocument != null
                && borrower.supportingDocument?.documentType != DocumentType.URL
            ) {

                deleteFileFromFirebaseStorage(
                    context,
                    borrower.supportingDocument?.documentUrl!!
                )
            }

            // all the payments related to this borrower
            val keysAndSupportingDocs =
                borrowerPaymentRepository.getPaymentKeysAndSupportingDocumentByBorrowerKey(borrower.key)

            val keys = keysAndSupportingDocs.map { it.key }
            val supportingDocument = keysAndSupportingDocs.map { it.supportingDocument }
                .filter { it != null && it.documentType != DocumentType.URL }

            supportingDocument.forEach { supportingDoc ->

                supportingDoc?.let { deleteFileFromFirebaseStorage(context, it.documentUrl) }
            }

            if (keys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    BORROWER_PAYMENTS,
                    convertStringListToJSON(keys)
                )
            }

            // all the partial payment related to this borrower
            val partialPaymentKeys =
                partialPaymentRepository.getKeysByBorrowerId(borrower.borrowerId)

            if (partialPaymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    PARTIAL_PAYMENTS,
                    convertStringListToJSON(partialPaymentKeys)
                )
            }
        }

        borrowerPaymentRepository.deleteAllBorrowerPaymentsByBorrowerKey(borrower.key)
        partialPaymentRepository.deleteAllPartialPaymentByBorrowerId(borrower.borrowerId)
        borrowerRepository.delete(borrower)
        getAllBorrower()
    }

    fun deleteAllBorrower() = viewModelScope.launch {

        borrowerRepository.deleteAllBorrower()
        borrowerPaymentRepository.deleteAllBorrowerPayments()
        partialPaymentRepository.deleteAllPartialPayments()
    }

    fun deleteBorrowerByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        borrowerRepository.deleteBorrowerByIsSynced(isSynced)
    }

    private val _allBorrowersList = MutableLiveData<List<Borrower>>()

    val allBorrowersList: LiveData<List<Borrower>> get() = _allBorrowersList

    fun getAllBorrower() {

        // Adding the interest amount to the borrower total due amount dynamically when user request for borrower list
        viewModelScope.launch {

            val allBorrowers = borrowerRepository.getAllBorrower().first()

            allBorrowers.forEach { borrower ->

                borrower.totalDueAmount = 0.0

                val paymentsList =
                    borrowerPaymentRepository.getPaymentsByBorrowerKey(borrower.key).first()
                        .filter { !it.isDueCleared && it.dueLeftAmount != 0.0 }

                paymentsList.forEach { payment ->

                    borrower.totalDueAmount += if (!payment.isInterestAdded) {

                        payment.dueLeftAmount
                    } else {

                        val partialPayments =
                            partialPaymentRepository.getPartialPaymentByBorrowerPaymentKey(payment.key)
                                .first()

                        payment.totalAmountPaid =
                            partialPayments.fold(0.0) { acc, partialPayment -> acc + partialPayment.amount }

                        var due = if (payment.isDueCleared) {

                            0.0
                        } else {

                            payment.amountTakenOnRent - payment.totalAmountPaid
                        }

                        if (payment.interest != null) {

                            val interestAndAmount = calculateInterestAndAmount(
                                InterestCalculatorFields(
                                    0L, payment.amountTakenOnRent, payment.interest!!,
                                    calculateNumberOfDays(
                                        payment.created,
                                        System.currentTimeMillis()
                                    )
                                )
                            )

                            due += interestAndAmount.first
                        }

                        due
                    }
                }
            }

            _allBorrowersList.value = allBorrowers
        }
    }

    fun getBorrowerByKey(borrowerKey: String) =
        borrowerRepository.getBorrowerByKey(borrowerKey).asLiveData()

}