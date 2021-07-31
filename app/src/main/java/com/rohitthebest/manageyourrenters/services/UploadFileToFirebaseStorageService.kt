package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.FILE_NAME_KEY
import com.rohitthebest.manageyourrenters.others.Constants.FILE_URI_KEY
import com.rohitthebest.manageyourrenters.others.Constants.UPLOAD_DATA_KEY
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.fromStringToBorrowerPayment
import com.rohitthebest.manageyourrenters.utils.insertToFireStore
import com.rohitthebest.manageyourrenters.utils.uploadFileUriOnFirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "UploadFileToFirebaseSto"

const val PROGRESS_MAX = 100
var PROGRESS_CURRENT = 0


@AndroidEntryPoint
class UploadFileToFirebaseStorageService : Service() {

    @Inject
    lateinit var borrowerPaymentRepository: BorrowerPaymentRepository

    @Inject
    lateinit var emiRepository: EMIRepository

    private var receivedCollection = ""
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private var notificationId = 100

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fileUri = intent?.getStringExtra(FILE_URI_KEY)
        val uploadData = intent?.getStringExtra(UPLOAD_DATA_KEY)
        val fileName = intent?.getStringExtra(FILE_NAME_KEY)
        receivedCollection = intent?.getStringExtra(COLLECTION_KEY)!! // pass collection name here

        notificationId = Random.nextInt(1000, 9999)

        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        notificationBuilder = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).apply {

            setSmallIcon(R.drawable.ic_document)
            setContentIntent(pendingIntent)
            setContentTitle("Uploading document to $receivedCollection.")
        }

        NotificationManagerCompat.from(this).apply {

            notificationBuilder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            notify(notificationId, notificationBuilder.build())
        }

        startForeground(notificationId, notificationBuilder.build())

        CoroutineScope(Dispatchers.IO).launch {

            val uri = Uri.parse(fileUri)

            when (receivedCollection) {

                getString(R.string.borrowerPayments) -> {

                    val borrowerPayment = fromStringToBorrowerPayment(uploadData!!)

                    val storageRef = FirebaseStorage.getInstance()
                        .getReference("${Functions.getUid()}/BorrowerPaymentDoc/${borrowerPayment.supportingDocument?.documentType}")

                    val fileRef = storageRef.child(fileName!!)

                    uploadFileToFirebaseStorage(
                        uri,
                        fileRef,
                        borrowerPayment
                    )
                }
            }

        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    private suspend inline fun uploadFileToFirebaseStorage(
        documentUri: Uri,
        fileReference: StorageReference,
        document: Any
    ) {

        uploadFileUriOnFirebaseStorage(
            documentUri,
            fileReference,
            {},
            { task ->

                try {

                    NotificationManagerCompat.from(applicationContext).apply {

                        PROGRESS_CURRENT =
                            ((100 * task.bytesTransferred) / task.totalByteCount).toInt()

                        notificationBuilder.setProgress(
                            PROGRESS_MAX,
                            PROGRESS_CURRENT,
                            false
                        )
                        notify(notificationId, notificationBuilder.build())
                    }

                    Log.d(
                        TAG,
                        "onStartCommand: file upload progress : $PROGRESS_CURRENT"
                    )
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

            },
            { downloadUrl ->

                CoroutineScope(Dispatchers.IO).launch {

                    var docKey = ""
                    val uploadDocument: Any

                    when (receivedCollection) {

                        getString(R.string.borrowerPayments) -> {

                            val borrowerPayment = document as BorrowerPayment
                            borrowerPayment.supportingDocument?.documentUrl = downloadUrl
                            docKey = borrowerPayment.key
                            borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)

                            uploadDocument = borrowerPayment
                        }

                        getString(R.string.emi) -> {

                            val emi = document as EMI
                            emi.supportingDocument.documentUrl = downloadUrl
                            docKey = emi.key
                            emiRepository.updateEMI(emi)

                            uploadDocument = emi
                        }

                        else -> uploadDocument = document
                    }

                    val docRef = FirebaseFirestore.getInstance()
                        .collection(receivedCollection)
                        .document(docKey)

                    if (insertToFireStore(docRef, uploadDocument)) {

                        NotificationManagerCompat.from(applicationContext).apply {

                            notificationBuilder
                                .setContentText("Upload complete")
                                .setProgress(0, 0, false)

                            notify(notificationId, notificationBuilder.build())
                        }

                        stopSelf()
                    }

                }

            },
            {

            },
            {

            }
        )
    }
}
