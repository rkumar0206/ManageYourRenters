package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.deleteAllDocumentsUsingKeyFromFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerPaymentViewMode"

@HiltViewModel
class BorrowerPaymentViewModel @Inject constructor(
    private val repository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository
) : ViewModel() {

    fun insertBorrowerPayment(borrowerPayment: BorrowerPayment) = viewModelScope.launch {
        repository.insertBorrowerPayment(borrowerPayment)
    }

    fun insertBorrowerPayments(borrowerPayments: List<BorrowerPayment>) = viewModelScope.launch {
        repository.insertAllBorrowerPayment(borrowerPayments)
    }

    fun updateBorrowerPayment(borrowerPayment: BorrowerPayment) = viewModelScope.launch {
        repository.updateBorrowerPayment(borrowerPayment)
    }

    fun deleteBorrowerPayment(context: Context, borrowerPayment: BorrowerPayment) =
        viewModelScope.launch {

            repository.deleteBorrowerPayment(borrowerPayment)

            val partialPaymentKeys =
                partialPaymentRepository.getKeysByBorrowerPaymentKey(borrowerPayment.key)

            Log.d(TAG, "deleteBorrowerPayment: Partial payment keys : $partialPaymentKeys")

            if (Functions.isInternetAvailable(context)) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.partialPayments),
                    convertStringListToJSON(partialPaymentKeys)
                )

            }

            partialPaymentRepository.deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPayment.key)
        }

    fun deleteAllBorrowerPayments() = viewModelScope.launch {
        repository.deleteAllBorrowerPayments()
    }

    fun deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey: String) = viewModelScope.launch {

        repository.deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey)
    }

    fun deleteBorrowerPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        repository.deleteBorrowerPaymentsByIsSynced(isSynced)
    }


    fun getAllBorrowerPayments() = repository.getAllBorrowerPayments().asLiveData()

    fun getPaymentsByBorrowerKey(borrowerKey: String) =
        repository.getPaymentsByBorrowerKey(borrowerKey).asLiveData()

    fun getTotalDueOfTheBorrower(borrowerKey: String) =
        repository.getTotalDueOfTheBorrower(borrowerKey).asLiveData()

    fun getBorrowerPaymentByKey(paymentKey: String) =
        repository.getBorrowerPaymentByKey(paymentKey).asLiveData()

    fun getPaymentKeysByBorrowerKey(borrowerKey: String) =
        repository.getPaymentKeysByBorrowerKey(borrowerKey).asLiveData()
}