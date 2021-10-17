package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.repositories.PaymentRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.deleteAllDocumentsUsingKeyFromFirestore
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RenterViewModel"

@HiltViewModel
class RenterViewModel @Inject constructor(
    private val repo: RenterRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    fun insertRenter(renter: Renter) = viewModelScope.launch {

        repo.insertRenter(renter)
    }

    fun insertRenters(renters: List<Renter>) = viewModelScope.launch {

        repo.insertRenters(renters)
    }

    fun deleteRenter(context: Context, renter: Renter) = viewModelScope.launch {

        val paymentKeys = paymentRepository.getPaymentKeysByRenterKey(renter.key!!)

        Log.d(TAG, "deleteRenter: PaymentsKeys : $paymentKeys")

        if (Functions.isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.renters),
                renter.key!!
            )

            if (paymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.payments),
                    convertStringListToJSON(paymentKeys)
                )
            }
        }

        repo.deleteRenter(renter)
        paymentRepository.deleteAllPaymentsOfRenter(renterKey = renter.key!!)
    }

    fun deleteAllRenter() = viewModelScope.launch {

        repo.deleteAllRenter()
        paymentRepository.deleteAllPayments()
    }

    fun deleteRenterByIsSynced(isSynced: String) = viewModelScope.launch {

        repo.deleteRenterByIsSynced(isSynced)
    }

    fun getAllRentersList() = repo.getAllRentersList()

    fun getRenterCount() = repo.getRenterCount()

    fun getRenterByIsSynced(isSynced: String) = repo.getRenterByIsSynced(isSynced)

    fun getRenterByKey(renterKey: String) = repo.getRenterByKey(renterKey)
}