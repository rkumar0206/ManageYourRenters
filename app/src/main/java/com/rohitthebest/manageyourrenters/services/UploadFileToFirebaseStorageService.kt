package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.FILE_NAME_KEY
import com.rohitthebest.manageyourrenters.others.Constants.FILE_URI_KEY
import com.rohitthebest.manageyourrenters.others.Constants.UPLOAD_DATA_KEY
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.fromStringToBorrowerPayment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "UploadFileToFirebaseSto"

@AndroidEntryPoint
class UploadFileToFirebaseStorageService : Service() {

    @Inject
    lateinit var borrowerPaymentRepository: BorrowerPaymentRepository

    @Inject
    lateinit var emiRepository: BorrowerRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fileUri = intent?.getStringExtra(FILE_URI_KEY)
        val uploadData = intent?.getStringExtra(UPLOAD_DATA_KEY)
        val fileName = intent?.getStringExtra(FILE_NAME_KEY)
        val collection = intent?.getStringExtra(COLLECTION_KEY) // pass collection name here

        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_document)
            .setContentIntent(pendingIntent)
            .setContentTitle("Uploading to $collection.")
            .build()

        startForeground(Random.nextInt(1000, 9999), notification)

        CoroutineScope(Dispatchers.IO).launch {

            val uri = Uri.parse(fileUri)

            when (collection) {

                getString(R.string.borrowerPayments) -> {

                    val borrowerPayment = fromStringToBorrowerPayment(uploadData!!)

                    val storageRef = FirebaseStorage.getInstance()
                        .getReference("${Functions.getUid()}/BorrowerPaymentDoc/${borrowerPayment.supportingDocument?.documentType}")

                    val fileRef = storageRef.child(fileName!!)

                    uploadFile(
                        uri,
                        fileRef,
                        {

                        },
                        { task ->

                            try {
                                val progress = (100 * task.bytesTransferred) / task.totalByteCount

                                Log.d(TAG, "onStartCommand: file upload progress : $progress")
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        },
                        { downloadUrl ->

                            CoroutineScope(Dispatchers.IO).launch {

                                borrowerPayment.supportingDocument?.documentUrl = downloadUrl

                                borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)

                                val docRef = FirebaseFirestore.getInstance()
                                    .collection(collection)
                                    .document(borrowerPayment.key)

                                if (insertToFireStore(docRef, borrowerPayment)) {

                                    Log.d(
                                        TAG,
                                        "onStartCommand: Borrower payment inserted to $collection"
                                    )

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

        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}

private suspend fun insertToFireStore(docRef: DocumentReference, data: Any): Boolean {

    return try {

        Log.i(TAG, "insertToFireStore")

        docRef.set(data)
            .await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}


suspend inline fun uploadFile(
    documentUri: Uri,
    fileReference: StorageReference,  // should be complete reference like mStorageRef.child(fileName)
    crossinline uploadTask: (UploadTask) -> Unit,
    crossinline progressListener: (UploadTask.TaskSnapshot) -> Unit,
    crossinline completeListener: (String) -> Unit,  // sending download url
    crossinline successListener: (Uri) -> Unit,
    crossinline failureListener: (Exception) -> Unit
) {

    fileReference.putFile(documentUri).let { mUploadTask ->

        uploadTask(mUploadTask)

        mUploadTask.addOnProgressListener { taskSnapshot -> // UploadTask.TaskSnapshot

            progressListener(taskSnapshot)
        }.continueWithTask { task -> // Task<UploadTask.TaskSnapshot!>

            if (!task.isSuccessful) {
                task.exception?.let { exception ->
                    throw exception
                }
            }
            fileReference.downloadUrl
        }.addOnCompleteListener { task -> // Task<Uri!>

            if (task.isSuccessful) {

                completeListener(task.result.toString())  // download url
            } else {

                task.exception?.let { exception ->
                    throw exception
                }
            }

        }.addOnSuccessListener { uri -> // Uri

            successListener(uri)
        }.addOnFailureListener { exception ->

            failureListener(exception)
        }
    }
        .await()
}