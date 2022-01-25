package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerViewModel"

@HiltViewModel
class BorrowerViewModel @Inject constructor(
    private val borrowerRepository: BorrowerRepository,
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository
) : ViewModel() {


    fun insertBorrower(context: Context, borrower: Borrower) = viewModelScope.launch {

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

        borrowerRepository.insertBorrower(borrower)
    }

    fun insertBorrowers(borrowers: List<Borrower>) = viewModelScope.launch {

        borrowerRepository.insertBorrowers(borrowers)
    }

    fun updateBorrower(context: Context, borrower: Borrower) = viewModelScope.launch {

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

        borrowerRepository.update(borrower)
    }

    fun deleteBorrower(context: Context, borrower: Borrower) = viewModelScope.launch {

        // all the payments related to this borrower
        val borrowerPaymentKeys =
            borrowerPaymentRepository.getPaymentKeysByBorrowerKey(borrower.key)
        val borrowerPayments = borrowerPaymentRepository.getPaymentsByBorrowerKey(borrower.key)

        // all the partial payment related to this borrower
        val partialPaymentKeys = partialPaymentRepository.getKeysByBorrowerId(borrower.borrowerId)

        if (Functions.isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.borrowers),
                borrower.key
            )

            if (borrowerPaymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    convertStringListToJSON(borrowerPaymentKeys)
                )

                CoroutineScope(Dispatchers.IO).launch {

                    Log.d(TAG, "deleteBorrower: Deleting supporting document coroutine scope")
                    // checking if the payment contains any supporting document,
                    // and if it contains, deleting it from the firebase storage
                    borrowerPayments.collect { payments ->

                        Log.d(TAG, "deleteBorrower: supporting document payment collect")
                        payments.forEach { payment ->

                            if (payment.isSupportingDocAdded
                                && payment.supportingDocument?.documentType != DocumentType.URL
                            ) {
                                payment.supportingDocument?.documentUrl?.let { docUrl ->
                                    deleteFileFromFirebaseStorage(
                                        context,
                                        docUrl
                                    )
                                }
                            }
                        }
                        return@collect
                    }
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