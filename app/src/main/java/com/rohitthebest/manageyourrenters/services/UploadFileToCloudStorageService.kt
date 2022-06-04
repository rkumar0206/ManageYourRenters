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
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.SHORTCUT_BORROWERS
import com.rohitthebest.manageyourrenters.others.Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadFileUriOnFirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "UploadFileToCloudStorageService"

@AndroidEntryPoint
class UploadFileToCloudStorageService : Service() {

    @Inject
    lateinit var borrowerRepository: BorrowerRepository

    @Inject
    lateinit var renterRepository: RenterRepository

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
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                notificationIntent.action = SHORTCUT_BORROWERS
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        notificationBuilder = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).apply {

            setSmallIcon(R.drawable.ic_house_renters)
            setContentIntent(pendingIntent)
            setContentTitle("Uploading supporting document to cloud.")
        }

        NotificationManagerCompat.from(this).apply {

            notificationBuilder.setProgress(100, 0, false)
            notify(notificationId, notificationBuilder.build())
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
                        notify(notificationId, notificationBuilder.build())
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

            },
            { downloadUrl ->

                val supportingDocument = SupportingDocument(
                    supportingDocumentHelperModel.documentName,
                    downloadUrl,
                    supportingDocumentHelperModel.documentType
                )

                val map = HashMap<String, Any?>()
                map["supportingDocument"] = supportingDocument

                val docRef = FirebaseFirestore.getInstance()
                    .collection(supportingDocumentHelperModel.modelName)
                    .document(documentKey)

                CoroutineScope(Dispatchers.IO).launch {

                    if (updateDocumentOnFireStore(docRef, map)) {

                        // insert to local database
                        when (supportingDocumentHelperModel.modelName) {

                            getString(R.string.borrowers) -> {

                                val borrower =
                                    borrowerRepository.getBorrowerByKey(documentKey).first()

                                borrower.supportingDocument = supportingDocument

                                borrowerRepository.update(borrower)

                                updateFinalNotificationAndStopService()
                            }
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

                Functions.showToast(
                    applicationContext,
                    "File could not upload successfully. Try again..."
                )

                stopSelf()
            }
        )

    }

    private fun updateFinalNotificationAndStopService() {

        NotificationManagerCompat.from(applicationContext).apply {

            notificationBuilder
                .setContentText("Upload complete")
                .setProgress(0, 0, false)

            notify(notificationId, notificationBuilder.build())
        }

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}