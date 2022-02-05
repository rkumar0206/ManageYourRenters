package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
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
    private val paymentRepository: RenterPaymentRepository,
    private val renterRepository: RenterRepository,
    private val state: SavedStateHandle
) : ViewModel() {

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

    fun insertPayment(context: Context, renterPayment: RenterPayment) = viewModelScope.launch {

        updateRenterDuesOrAdvance(context, renterPayment, renterPayment.renterKey)

        if (isInternetAvailable(context)) {

            renterPayment.isSynced = true
            uploadDocumentToFireStore(
                context,
                context.getString(R.string.renter_payments),
                renterPayment.key
            )
        } else {

            renterPayment.isSynced = false
        }

        paymentRepository.insertRenterPayment(renterPayment)
    }

    private fun updateRenterDuesOrAdvance(
        context: Context,
        renterPayment: RenterPayment?,
        renterKey: String
    ) =

        viewModelScope.launch {

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


    fun updatePayment(context: Context, renterPayment: RenterPayment) = viewModelScope.launch {

        uploadDocumentToFireStore(
            context,
            context.getString(R.string.renter_payments),
            renterPayment.key
        )

        paymentRepository.updateRenterPayment(renterPayment)
    }

    fun insertPayments(renterPayments: List<RenterPayment>) = viewModelScope.launch {

        paymentRepository.insertAllRenterPayment(renterPayments)
    }

    fun deletePayment(context: Context, renterPayment: RenterPayment) = viewModelScope.launch {

        val renterKey = renterPayment.renterKey

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.renter_payments),
                renterPayment.key
            )
        }

        paymentRepository.deleteRenterPayment(renterPayment)

        // update renter's due or advance from last payment after deleting this payment
        val lastPayment = paymentRepository.getLastRenterPayment(renterKey).first()

        updateRenterDuesOrAdvance(context, lastPayment, renterKey)
    }

    fun deleteAllPaymentsOfRenter(context: Context, renterKey: String) = viewModelScope.launch {

        val paymentKeys = paymentRepository.getPaymentKeysByRenterKey(renterKey)

        if (isInternetAvailable(context)) {

            if (paymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.renter_payments),
                    convertStringListToJSON(paymentKeys)
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