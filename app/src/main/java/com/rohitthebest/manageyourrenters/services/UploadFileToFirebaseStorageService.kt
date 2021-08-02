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
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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

    @Inject
    lateinit var borrowerRepository: BorrowerRepository

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
            var referenceString = ""
            val document: Any

            when (receivedCollection) {

                getString(R.string.borrowerPayments) -> {

                    val borrowerPayment = fromStringToBorrowerPayment(uploadData!!)

                    referenceString =
                        "${Functions.getUid()}/BorrowerPaymentDoc/${borrowerPayment.supportingDocument?.documentType}"

                    document = borrowerPayment
                }

                getString(R.string.emis) -> {

                    val emi = fromStringToEMI(uploadData!!)

                    referenceString =
                        "${Functions.getUid()}/EMIDoc/${emi.supportingDocument?.documentType}"

                    document = emi
                }

                else -> document = Any()
            }

            val storageRef = FirebaseStorage.getInstance()
                .getReference(referenceString)

            val fileRef = storageRef.child(fileName!!)

            uploadFileToFirebaseStorage(
                uri,
                fileRef,
                document
            )

        }

        return START_NOT_STICKY
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

                // updating progress here

                try {

                    NotificationManagerCompat.from(applicationContext).apply {

                        PROGRESS_CURRENT =
                            ((100 * task.bytesTransferred) / task.totalByteCount).toInt()

                        notificationBuilder.priority = NotificationCompat.PRIORITY_LOW
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

                // inserting downloadUrl to the database document

                CoroutineScope(Dispatchers.IO).launch {

                    when (receivedCollection) {

                        getString(R.string.borrowerPayments) -> {

                            val borrowerPayment = document as BorrowerPayment
                            borrowerPayment.isSynced = true
                            borrowerPayment.supportingDocument?.documentUrl = downloadUrl

                            borrowerPaymentRepository.insertBorrowerPayment(borrowerPayment)
                            insertDocument(borrowerPayment, borrowerPayment.key)
                            updateBorrowerDueAmount(borrowerPayment)
                        }

                        getString(R.string.emis) -> {

                            val emi = document as EMI
                            emi.supportingDocument?.documentUrl = downloadUrl
                            emi.isSynced = true

                            emiRepository.insertEMI(emi)
                            insertDocument(emi, emi.key)
                        }
                    }
                }
            },
            {

                Log.d(TAG, "uploadFileToFirebaseStorage: Successful file upload")
            },
            {

                Log.d(TAG, "uploadFileToFirebaseStorage: Something went wrong...")
                Log.d(TAG, "uploadFileToFirebaseStorage: Exception : ${it.message}")

                showToast(applicationContext, "File could not upload successfully. Try again...")
                stopSelf()
            }
        )
    }

    private suspend fun insertDocument(uploadDocument: Any, docKey: String) {

        val docRef = FirebaseFirestore.getInstance()
            .collection(receivedCollection)
            .document(docKey)

        Log.d(TAG, "uploadFileToFirebaseStorage: docRef : $docRef")

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

    private suspend fun updateBorrowerDueAmount(borrowerPayment: BorrowerPayment) {

        borrowerRepository.getBorrowerByKey(borrowerPayment.borrowerKey).collect { borrower ->

            borrowerPaymentRepository.getTotalDueOfTheBorrower(borrowerPayment.borrowerKey)
                .collect { value ->

                    borrower.totalDueAmount = value
                    borrower.modified = System.currentTimeMillis()

                    val map = HashMap<String, Any?>()
                    map["totalDueAmount"] = value

                    val docRef = FirebaseFirestore.getInstance()
                        .collection(applicationContext.getString(R.string.borrowers))
                        .document(borrower.key)

                    if (borrower.isSynced) {

                        // if the document was synced previously update the document else insert
                        // it to the firestore
                        updateDocumentOnFireStore(
                            docRef,
                            map
                        )

                    } else {

                        borrower.isSynced = true

                        insertToFireStore(
                            docRef,
                            borrower
                        )
                    }

                    borrowerRepository.update(borrower)
                }
        }

    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

}
