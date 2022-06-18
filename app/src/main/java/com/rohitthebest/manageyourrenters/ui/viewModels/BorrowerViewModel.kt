package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerViewModel"

@HiltViewModel
class BorrowerViewModel @Inject constructor(
    app: Application,
    private val borrowerRepository: BorrowerRepository,
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository
) : AndroidViewModel(app) {

    fun insertBorrower(
        borrower: Borrower,
        supportDocumentHelper: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            borrower.isSynced = true

            uploadDocumentToFireStore(
                context,
                context.getString(R.string.borrowers),
                borrower.key
            )

            if (supportDocumentHelper != null
                && supportDocumentHelper.documentType != DocumentType.URL
            ) {

                supportDocumentHelper.modelName = context.getString(R.string.borrowers)
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
                context.getString(R.string.borrowers),
                borrower.key
            )
        } else {

            borrower.isSynced = false
        }

        borrowerRepository.update(borrower)
    }

    fun addOrReplaceBorrowerSupportingDocument(
        borrower: Borrower,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

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

            supportDocumentHelper.modelName = context.getString(R.string.borrowers)

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
                context.getString(R.string.borrowers),
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
                    context.getString(R.string.borrowerPayments),
                    convertStringListToJSON(keys)
                )
            }

            // all the partial payment related to this borrower
            val partialPaymentKeys =
                partialPaymentRepository.getKeysByBorrowerId(borrower.borrowerId)

            if (partialPaymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.partialPayments),
                    convertStringListToJSON(partialPaymentKeys)
                )
            }
        }

        borrowerPaymentRepository.deleteAllBorrowerPaymentsByBorrowerKey(borrower.key)
        partialPaymentRepository.deleteAllPartialPaymentByBorrowerId(borrower.borrowerId)
        borrowerRepository.delete(borrower)
    }

    fun deleteAllBorrower() = viewModelScope.launch {

        borrowerRepository.deleteAllBorrower()
        borrowerPaymentRepository.deleteAllBorrowerPayments()
        partialPaymentRepository.deleteAllPartialPayments()
    }

    fun deleteBorrowerByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        borrowerRepository.deleteBorrowerByIsSynced(isSynced)
    }

    fun getAllBorrower() = borrowerRepository.getAllBorrower().asLiveData()

    fun getBorrowerByKey(borrowerKey: String) =
        borrowerRepository.getBorrowerByKey(borrowerKey).asLiveData()

}