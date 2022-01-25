package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerPaymentViewMode"

@HiltViewModel
class BorrowerPaymentViewModel @Inject constructor(
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository,
    private val borrowerRepository: BorrowerRepository
) : ViewModel() {

    fun insertBorrowerPayment(context: Context, borrowerPayment: BorrowerPayment) =
        viewModelScope.launch {

            borrowerPaymentRepository.insertBorrowerPayment(borrowerPayment)

            delay(50)

            // update the borrower due

            updateBorrowerDueAmount(context, borrowerPayment.borrowerKey)
        }

    private var isRefreshEnabled = true

    private suspend fun updateBorrowerDueAmount(context: Context, borrowerKey: String) {

        borrowerRepository.getBorrowerByKey(borrowerKey).collect { borrower ->

            if (isRefreshEnabled) {

                try {

                    borrowerPaymentRepository.getTotalDueOfTheBorrower(borrowerKey)
                        .collect { value ->

                            isRefreshEnabled = false
                            proceedUpdate(context, borrower, value)

                        }
                } catch (e: NullPointerException) {

                    // The last borrower payment has been deleted and therefore the collect block
                    // throws null pointer exception
                    isRefreshEnabled = false
                    proceedUpdate(context, borrower, 0.0)

                    e.printStackTrace()
                }
            }
        }

    }

    private suspend fun proceedUpdate(context: Context, borrower: Borrower, value: Double) {

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

    fun updateBorrowerPayment(borrowerPayment: BorrowerPayment) = viewModelScope.launch {
        borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)
    }

    fun deleteBorrowerPayment(context: Context, borrowerPayment: BorrowerPayment) =
        viewModelScope.launch {

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

            isRefreshEnabled = true
            updateBorrowerDueAmount(context, borrowerPayment.borrowerKey)
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