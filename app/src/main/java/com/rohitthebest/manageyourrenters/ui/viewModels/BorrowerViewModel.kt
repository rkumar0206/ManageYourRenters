package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.deleteAllDocumentsUsingKeyFromFirestore
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BorrowerViewModel @Inject constructor(
    private val borrowerRepository: BorrowerRepository,
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository
) : ViewModel() {


    fun insertBorrower(borrower: Borrower) = viewModelScope.launch {

        borrowerRepository.insertBorrower(borrower)
    }

    fun insertBorrowers(borrowers: List<Borrower>) = viewModelScope.launch {

        borrowerRepository.insertBorrowers(borrowers)
    }

    fun updateBorrower(borrower: Borrower) = viewModelScope.launch {

        borrowerRepository.update(borrower)
    }

    fun deleteBorrower(context: Context, borrower: Borrower) = viewModelScope.launch {

        // all the payments related to this borrower
        val borrowerPaymentKeys =
            borrowerPaymentRepository.getPaymentKeysByBorrowerKey(borrower.key)

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

            }

            if (partialPaymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.partialPayments),
                    convertStringListToJSON(partialPaymentKeys)
                )
            }
        }

        borrowerRepository.delete(borrower)
        borrowerPaymentRepository.deleteAllBorrowerPaymentsByBorrowerKey(borrower.key)
        partialPaymentRepository.deleteAllPartialPaymentByBorrowerId(borrower.borrowerId)

    }

    fun deleteAllBorrower() = viewModelScope.launch {

        borrowerRepository.deleteAllBorrower()
    }

    fun deleteBorrowerByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        borrowerRepository.deleteBorrowerByIsSynced(isSynced)
    }

    fun getAllBorrower() = borrowerRepository.getAllBorrower().asLiveData()

    fun getBorrowerByKey(borrowerKey: String) =
        borrowerRepository.getBorrowerByKey(borrowerKey).asLiveData()

    fun getBorrowerByIsSynced(isSynced: Boolean) =
        borrowerRepository.getBorrowerByIsSynced(isSynced).asLiveData()

}