package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerPaymentViewMode"

@HiltViewModel
class BorrowerPaymentViewModel @Inject constructor(
    app: Application,
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository,
    private val borrowerRepository: BorrowerRepository
) : AndroidViewModel(app) {

    fun insertBorrowerPayment(
        borrowerPayment: BorrowerPayment,
        supportingDocumentHelperModel: SupportingDocumentHelperModel? = null
    ) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                borrowerPayment.isSynced = true

                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )

                if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL
                ) {
                    supportingDocumentHelperModel.modelName =
                        context.getString(R.string.borrowerPayments)
                    uploadFileToFirebaseCloudStorage(
                        context, supportingDocumentHelperModel, borrowerPayment.key
                    )
                }

            } else {

                borrowerPayment.isSynced = false
            }

            borrowerPaymentRepository.insertBorrowerPayment(borrowerPayment)

            delay(50)

            // update the borrower due
            updateBorrowerDueAmount(borrowerPayment.borrowerKey)
        }

    private suspend fun updateBorrowerDueAmount(borrowerKey: String) {

        val borrower = borrowerRepository.getBorrowerByKey(borrowerKey).first()

        try {
            val totalDue = borrowerPaymentRepository.getTotalDueOfTheBorrower(borrowerKey).first()
            proceedUpdate(borrower, totalDue)
        } catch (e: NullPointerException) {

            proceedUpdate(borrower, 0.0)
        }

    }

    private suspend fun proceedUpdate(borrower: Borrower, value: Double) {

        val context = getApplication<Application>().applicationContext

        borrower.totalDueAmount = value
        borrower.modified = System.currentTimeMillis()

        if (isInternetAvailable(context)) {

            if (borrower.isSynced) {

                // if the borrower document was already synced previously then update the document
                // or else upload the entire document to the fireStore

                val map = HashMap<String, Any?>()
                map["totalDueAmount"] = value

                updateDocumentOnFireStore(
                    context,
                    map = map,
                    context.getString(R.string.borrowers),
                    borrower.key
                )
            } else {

                borrower.isSynced = true

                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.borrowers),
                    borrower.key
                )
            }
        } else {

            borrower.isSynced = false
        }

        borrowerRepository.update(borrower)

    }

    fun insertBorrowerPayments(borrowerPayments: List<BorrowerPayment>) = viewModelScope.launch {
        borrowerPaymentRepository.insertAllBorrowerPayment(borrowerPayments)
    }

    fun updateBorrowerPayment(
        oldBorrowerPayment: BorrowerPayment,
        borrowerPayment: BorrowerPayment
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            borrowerPayment.isSynced = true

            if (!oldBorrowerPayment.isSynced) {

                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )
            } else {

                val map =
                    compareBorrowerPaymentModel(oldBorrowerPayment, borrowerPayment)

                if (map.isNotEmpty()) {

                    updateDocumentOnFireStore(
                        context,
                        map,
                        context.getString(R.string.borrowerPayments),
                        borrowerPayment.key
                    )
                }
            }
        } else {
            borrowerPayment.isSynced = false
        }

        borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)

    }

    fun deleteBorrowerPayment(borrowerPayment: BorrowerPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            val partialPaymentKeys =
                partialPaymentRepository.getKeysByBorrowerPaymentKey(borrowerPayment.key)

            Log.d(TAG, "deleteBorrowerPayment: Partial payment keys : $partialPaymentKeys")

            if (isInternetAvailable(context)) {

                deleteDocumentFromFireStore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )

                if (borrowerPayment.isSupportingDocAdded) {

                    if (borrowerPayment.supportingDocument?.documentType != DocumentType.URL) {

                        deleteFileFromFirebaseStorage(
                            context,
                            borrowerPayment.supportingDocument?.documentUrl!!
                        )
                    }
                }

                if (partialPaymentKeys.isNotEmpty()) {

                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        context.getString(R.string.partialPayments),
                        convertStringListToJSON(partialPaymentKeys)
                    )
                }
            }

            borrowerPaymentRepository.deleteBorrowerPayment(borrowerPayment)
            partialPaymentRepository.deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPayment.key)
            updateBorrowerDueAmount(borrowerPayment.borrowerKey)
        }


    fun addOrReplaceBorrowerSupportingDocument(
        borrowerPayment: BorrowerPayment,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

        val oldBorrowerPayment = borrowerPayment.copy()

        if (oldBorrowerPayment.supportingDocument != null && oldBorrowerPayment.supportingDocument?.documentType != DocumentType.URL) {

            // if borrower payment contains supporting document previously, then call delete service also

            deleteFileFromFirebaseStorage(
                context,
                borrowerPayment.supportingDocument?.documentUrl!!
            )
        }

        borrowerPayment.modified = System.currentTimeMillis()
        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            borrowerPayment.isSupportingDocAdded = true
            borrowerPayment.supportingDocument = supportingDoc

            updateBorrowerPayment(oldBorrowerPayment, borrowerPayment)
        } else {

            supportDocumentHelper.modelName = context.getString(R.string.borrowerPayments)

            if (!oldBorrowerPayment.isSynced) {
                insertBorrowerPayment(borrowerPayment, supportDocumentHelper)
                return
            }
            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, borrowerPayment.key
            )
        }
    }

    fun deleteAllBorrowerPayments() = viewModelScope.launch {
        borrowerPaymentRepository.deleteAllBorrowerPayments()
    }

    fun deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey: String) = viewModelScope.launch {

        borrowerPaymentRepository.deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey)
    }

    fun deleteBorrowerPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        borrowerPaymentRepository.deleteBorrowerPaymentsByIsSynced(isSynced)
    }

    fun getPaymentsByBorrowerKey(borrowerKey: String) =
        borrowerPaymentRepository.getPaymentsByBorrowerKey(borrowerKey).asLiveData()

    fun getTotalDueOfTheBorrower(borrowerKey: String) =
        borrowerPaymentRepository.getTotalDueOfTheBorrower(borrowerKey).asLiveData()

    fun getBorrowerPaymentByKey(paymentKey: String) =
        borrowerPaymentRepository.getBorrowerPaymentByKey(paymentKey).asLiveData()

}