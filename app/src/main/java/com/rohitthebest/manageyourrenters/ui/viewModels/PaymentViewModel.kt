package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.repositories.PaymentRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val renterRepository: RenterRepository
) : ViewModel() {

    fun insertPayment(context: Context, payment: Payment) = viewModelScope.launch {

        updateRenterDuesOrAdvance(context, payment, payment.renterKey)

        if (isInternetAvailable(context)) {

            payment.isSynced = context.getString(R.string.t)
            uploadDocumentToFireStore(
                context,
                context.getString(R.string.payments),
                payment.key
            )
        } else {

            payment.isSynced = context.getString(R.string.f)
        }

        paymentRepository.insertPayment(payment)
    }

    private fun updateRenterDuesOrAdvance(context: Context, payment: Payment?, renterKey: String) =

        viewModelScope.launch {

            val renter: Renter = renterRepository.getRenterByKey(renterKey).first()

            var amountPaid = 0.0
            var totalAmount = 0.0

            if (payment != null) {

                amountPaid = payment.amountPaid!!.toDouble()
                totalAmount = payment.totalRent.toDouble()
            }

            renter.modified = System.currentTimeMillis()
            renter.dueOrAdvanceAmount = amountPaid - totalAmount

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


    fun updatePayment(context: Context, payment: Payment) = viewModelScope.launch {

        uploadDocumentToFireStore(
            context,
            context.getString(R.string.payments),
            payment.key
        )

        paymentRepository.updatePayment(payment)
    }

    fun insertPayments(payments: List<Payment>) = viewModelScope.launch {

        paymentRepository.insertPayments(payments)
    }

    fun deletePayment(context: Context, payment: Payment) = viewModelScope.launch {

        val renterKey = payment.renterKey

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.payments),
                payment.key
            )
        }

        paymentRepository.deletePayment(payment)

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
                    context.getString(R.string.payments),
                    convertStringListToJSON(paymentKeys)
                )
            }
        }
        paymentRepository.deleteAllPaymentsOfRenter(renterKey)
    }

    fun deleteAllPaymentsByIsSynced(isSynced: String) = viewModelScope.launch {

        paymentRepository.deleteAllPaymentsByIsSynced(isSynced)
    }

    fun getAllPaymentsListOfRenter(renterKey: String) =
        paymentRepository.getAllPaymentsListOfRenter(renterKey).asLiveData()

    fun getPaymentByPaymentKey(paymentKey: String) =
        paymentRepository.getPaymentByPaymentKey(paymentKey).asLiveData()

    fun getLastRenterPayment(renterKey: String) =
        paymentRepository.getLastRenterPayment(renterKey).asLiveData()
}