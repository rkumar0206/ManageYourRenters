package com.rohitthebest.manageyourrenters.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.*
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.RANDOM_ID_KEY
import com.rohitthebest.manageyourrenters.repositories.*
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.insertToFireStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "UploadService"

@AndroidEntryPoint
class UploadService : Service() {

    @Inject
    lateinit var borrowerRepository: BorrowerRepository

    @Inject
    lateinit var borrowerPaymentRepository: BorrowerPaymentRepository

    @Inject
    lateinit var renterRepository: RenterRepository

    @Inject
    lateinit var renterPaymentRepository: PaymentRepository

    @Inject
    lateinit var emiRepository: EMIRepository

    @Inject
    lateinit var emiPaymentRepository: EMIPaymentRepository


    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1003)

        val image = R.drawable.ic_house_renters

        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentIntent(pendingIntent)
            .setContentTitle("Uploading to $collection.")
            .setProgress(100, 0, true)
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        CoroutineScope(Dispatchers.IO).launch {

            when (collection) {

                getString(R.string.renters) -> {

                    val renter = renterRepository.getRenterByKey(key).first()

                    updateIsSyncedValueOfDocument(
                        collection,
                        renter,
                        insertToFireStore(docRef, renter)
                    )
                    stopSelf()
                }

                getString(R.string.payments) -> {

                    val payment = borrowerPaymentRepository.getBorrowerPaymentByKey(key).first()

                    updateIsSyncedValueOfDocument(
                        collection,
                        payment,
                        insertToFireStore(docRef, payment)
                    )

                    stopSelf()
                }

                getString(R.string.borrowers) -> {

                    val borrower = borrowerRepository.getBorrowerByKey(key).first()

                    updateIsSyncedValueOfDocument(
                        collection,
                        borrower,
                        insertToFireStore(docRef, borrower)
                    )

                    stopSelf()
                }

                getString(R.string.borrowerPayments) -> {

                    val borrowerPayment =
                        borrowerPaymentRepository.getBorrowerPaymentByKey(key).first()

                    updateIsSyncedValueOfDocument(
                        collection,
                        borrowerPayment,
                        insertToFireStore(docRef, borrowerPayment)
                    )

                    stopSelf()
                }

                getString(R.string.emis) -> {

                    val emi = emiRepository.getEMIByKey(key).first()

                    updateIsSyncedValueOfDocument(
                        collection,
                        emi,
                        insertToFireStore(docRef, emi)
                    )

                    stopSelf()
                }

                getString(R.string.emiPayments) -> {

                    val emiPayment = emiPaymentRepository.getEMIPaymentByKey(key).first()

                    updateIsSyncedValueOfDocument(
                        collection,
                        emiPayment,
                        insertToFireStore(docRef, emiPayment)
                    )

                    stopSelf()
                }

                else -> {

                    stopSelf()
                }
            }

        }


        return START_NOT_STICKY
    }


    private suspend fun updateIsSyncedValueOfDocument(
        collection: String,
        document: Any,
        isSyncedValue: Boolean
    ) {

        when (collection) {

            getString(R.string.renters) -> {

                val renter = document as Renter

                renter.isSynced =
                    if (isSyncedValue) getString(R.string.t) else getString(R.string.f)

                renterRepository.updateRenter(renter)
            }

            getString(R.string.payments) -> {

                val payment = document as Payment
                payment.isSynced =
                    if (isSyncedValue) getString(R.string.t) else getString(R.string.f)
                renterPaymentRepository.updatePayment(payment)
            }

            getString(R.string.borrowers) -> {

                val borrower = document as Borrower
                borrower.isSynced = isSyncedValue
                borrowerRepository.update(borrower)
            }

            getString(R.string.borrowerPayments) -> {
                val borrowerPayment = document as BorrowerPayment
                borrowerPayment.isSynced = isSyncedValue
                borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)
            }

            getString(R.string.emis) -> {
                val emi = document as EMI
                emi.isSynced = isSyncedValue
                emiRepository.updateEMI(emi)
            }

            getString(R.string.emiPayments) -> {
                val emiPayment = document as EMIPayment
                emiPayment.isSynced = isSyncedValue
                emiPaymentRepository.updateEMIPayment(emiPayment)
            }

            else -> stopSelf()
        }

        if (isSyncedValue) {

            Log.d(
                TAG,
                "updateIsSyncedValueOfDocument: upload in collection $collection, was successful"
            )
        } else {

            Log.d(
                TAG,
                "updateIsSyncedValueOfDocument: upload in collection $collection, was UNSUCCESSFUL"
            )
        }


    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}
