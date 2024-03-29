package com.rohitthebest.manageyourrenters.services

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMI_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMIs
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.*
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadFileUriOnFirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "UploadFileToCloudStorageService"

@AndroidEntryPoint
class UploadFileToCloudStorageService : Service() {

    @Inject
    lateinit var borrowerRepository: BorrowerRepository

    @Inject
    lateinit var renterRepository: RenterRepository

    @Inject
    lateinit var borrowerPaymentRepository: BorrowerPaymentRepository

    @Inject
    lateinit var renterPaymentRepository: RenterPaymentRepository

    @Inject
    lateinit var emiRepository: EMIRepository

    @Inject
    lateinit var emiPaymentRepository: EMIPaymentRepository

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationId = 100

    private lateinit var supportingDocumentHelperModel: SupportingDocumentHelperModel
    private var documentKey = ""

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val supportingDocHelperModelString =
            intent?.getStringExtra(SUPPORTING_DOCUMENT_HELPER_MODEL_KEY)
        documentKey = intent?.getStringExtra(DOCUMENT_KEY)!!

        supportingDocumentHelperModel =
            supportingDocHelperModelString?.convertJsonToObject(SupportingDocumentHelperModel::class.java)!!

        notificationId = Random.nextInt(1000, 9999)

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this, supportingDocumentHelperModel.modelName
            )

        notificationBuilder = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).apply {

            setSmallIcon(R.drawable.ic_house_renters)
            setContentIntent(pendingIntent)
            setContentTitle(getString(R.string.uploading_supporting_document_to_cloud))
        }

        NotificationManagerCompat.from(this).apply {

            notificationBuilder.setProgress(100, 0, false)
            updateNotification(this, notificationId, notificationBuilder)
        }

        startForeground(notificationId, notificationBuilder.build())

        CoroutineScope(Dispatchers.IO).launch {

            // location of the document in the storage
            val referenceString =
                "${Functions.getUid()}/${supportingDocumentHelperModel.modelName}Doc/${supportingDocumentHelperModel.documentType}"

            val storageRef = FirebaseStorage.getInstance()
                .getReference(referenceString)

            val fileRef = storageRef.child(supportingDocumentHelperModel.documentName)

            uploadFileToFirebaseStorage(fileRef)
        }

        // starting stop timer for 3 minutes
        CoroutineScope(Dispatchers.IO).launch {

            Log.d(TAG, "onStartCommand: timer started for 3 minutes")
            delay(TimeUnit.MINUTES.toMillis(3))
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun uploadFileToFirebaseStorage(fileRef: StorageReference) {

        uploadFileUriOnFirebaseStorage(
            Uri.parse(supportingDocumentHelperModel.documentUri),
            fileRef,
            {},
            { task ->

                // updating progress here
                try {

                    NotificationManagerCompat.from(applicationContext).apply {

                        val currentProgress =
                            ((100 * task.bytesTransferred) / task.totalByteCount).toInt()

                        notificationBuilder.priority = NotificationCompat.PRIORITY_LOW
                        notificationBuilder.setProgress(
                            100,
                            currentProgress,
                            false
                        )
                        updateNotification(this, notificationId, notificationBuilder)
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

            },
            { downloadUrl ->

                CoroutineScope(Dispatchers.IO).launch {

                    val supportingDocument = SupportingDocument(
                        supportingDocumentHelperModel.documentName,
                        downloadUrl,
                        supportingDocumentHelperModel.documentType
                    )

                    val map = HashMap<String, Any?>()
                    map["supportingDocAdded"] = true
                    map["supportingDocument"] = supportingDocument

                    val docRef = FirebaseFirestore.getInstance()
                        .collection(supportingDocumentHelperModel.modelName)
                        .document(documentKey)

                    if (updateDocumentOnFireStore(docRef, map)) {

                        addSupportingDocumentDetailToLocalDb(supportingDocument)
                    }
                }

            },
            {

                Log.d(TAG, "uploadFileToFirebaseStorage: Successful file upload")
            },
            {

                Log.d(TAG, "uploadFileToFirebaseStorage: Something went wrong...")
                Log.d(TAG, "uploadFileToFirebaseStorage: Exception : ${it.message}")

                Functions.showToast(
                    applicationContext,
                    "File could not upload successfully. Try again..."
                )

                stopSelf()
            }
        )

    }

    private suspend fun addSupportingDocumentDetailToLocalDb(supportingDocument: SupportingDocument) {

        // insert to local database
        when (supportingDocumentHelperModel.modelName) {

            BORROWERS -> {

                val borrower =
                    borrowerRepository.getBorrowerByKey(documentKey).first()

                borrower.isSupportingDocAdded = true
                borrower.supportingDocument = supportingDocument

                borrowerRepository.update(borrower)

                updateFinalNotificationAndStopService()
            }

            RENTERS -> {

                val renter = renterRepository.getRenterByKey(documentKey).first()

                renter.isSupportingDocAdded = true
                renter.supportingDocument = supportingDocument

                renterRepository.updateRenter(renter)
                updateFinalNotificationAndStopService()
            }

            BORROWER_PAYMENTS -> {

                val payment = borrowerPaymentRepository.getBorrowerPaymentByKey(documentKey).first()

                payment.isSupportingDocAdded = true
                payment.supportingDocument = supportingDocument

                borrowerPaymentRepository.updateBorrowerPayment(payment)
                updateFinalNotificationAndStopService()
            }

            RENTER_PAYMENTS -> {

                val payment = renterPaymentRepository.getPaymentByPaymentKey(documentKey).first()

                payment.isSupportingDocAdded = true
                payment.supportingDocument = supportingDocument

                renterPaymentRepository.updateRenterPayment(payment)
                updateFinalNotificationAndStopService()
            }

            EMIs -> {

                val emi = emiRepository.getEMIByKey(documentKey).first()

                emi.isSupportingDocAdded = true
                emi.supportingDocument = supportingDocument

                emiRepository.updateEMI(emi)
                updateFinalNotificationAndStopService()
            }

            EMI_PAYMENTS -> {

                val emiPayment = emiPaymentRepository.getEMIPaymentByKey(documentKey).first()

                emiPayment.isSupportingDocAdded = true
                emiPayment.supportingDocument = supportingDocument

                emiPaymentRepository.updateEMIPayment(emiPayment)
                updateFinalNotificationAndStopService()
            }
        }
    }

    private fun updateFinalNotificationAndStopService() {

        NotificationManagerCompat.from(applicationContext).apply {

            notificationBuilder
                .setContentText("Upload complete")
                .setProgress(0, 0, false)

            updateNotification(this, notificationId, notificationBuilder)
        }

        stopSelf()
    }

    private fun updateNotification(
        notificationManagerCompat: NotificationManagerCompat,
        notificationId: Int,
        notificationBuilder: NotificationCompat.Builder
    ) {
        if (ActivityCompat.checkSelfPermission(
                this@UploadFileToCloudStorageService,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManagerCompat.notify(notificationId, notificationBuilder.build())
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}