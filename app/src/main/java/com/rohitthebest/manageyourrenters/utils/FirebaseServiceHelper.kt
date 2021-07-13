package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.services.DeleteAllDocumentsService
import com.rohitthebest.manageyourrenters.services.DeleteService
import com.rohitthebest.manageyourrenters.services.UpdateService
import com.rohitthebest.manageyourrenters.services.UploadService
import kotlin.random.Random

fun uploadDocumentToFireStore(
    context: Context,
    uploadData: String,
    collection: String,
    documentKey: String
) {

    val foregroundService = Intent(context, UploadService::class.java)

    foregroundService.putExtra(
        Constants.COLLECTION_KEY,
        collection
    )

    foregroundService.putExtra(
        Constants.DOCUMENT_KEY,
        documentKey
    )

    foregroundService.putExtra(
        Constants.UPLOAD_DATA_KEY,
        uploadData
    )

    foregroundService.putExtra(
        Constants.RANDOM_ID_KEY,
        Random.nextInt(1000, 9999)
    )

    ContextCompat.startForegroundService(context, foregroundService)
}

fun updateDocumentOnFireStore(
    context: Context,
    map: HashMap<String, Any?>?,
    collectionKey: String,
    documentKey: String
) {

    val foregroundService = Intent(context, UpdateService::class.java)

    foregroundService.putExtra(
        Constants.COLLECTION_KEY,
        collectionKey
    )

    foregroundService.putExtra(
        Constants.DOCUMENT_KEY,
        documentKey
    )

    foregroundService.putExtra(
        Constants.UPDATE_DOCUMENT_MAP_KEY,
        map
    )

    foregroundService.putExtra(
        Constants.RANDOM_ID_KEY,
        Random.nextInt(1000, 9999)
    )

    ContextCompat.startForegroundService(context, foregroundService)
}


fun deleteDocumentFromFireStore(
    context: Context,
    collection: String,
    documentKey: String
) {

    val foregroundService = Intent(context, DeleteService::class.java)

    foregroundService.putExtra(
        Constants.COLLECTION_KEY,
        collection
    )

    foregroundService.putExtra(
        Constants.DOCUMENT_KEY,
        documentKey
    )

    foregroundService.putExtra(
        Constants.RANDOM_ID_KEY,
        Random.nextInt(1000, 9999)
    )

    ContextCompat.startForegroundService(context, foregroundService)
}

fun deleteAllDocumentsUsingKey(
    context: Context,
    collection: String,
    keyList: String
) {

    val foregroundService = Intent(context, DeleteAllDocumentsService::class.java)

    foregroundService.putExtra(
        Constants.COLLECTION_KEY,
        collection
    )

    foregroundService.putExtra(
        Constants.KEY_LIST_KEY,
        keyList
    )

    foregroundService.putExtra(
        Constants.RANDOM_ID_KEY,
        Random.nextInt(1000, 9999)
    )

    ContextCompat.startForegroundService(context, foregroundService)

}

inline fun uploadFileToFirebaseStorage(
    documentUri: Uri,
    fileReference: StorageReference,  // should be complete reference like mStorageRef.child(fileName)
    crossinline progressListener: (UploadTask.TaskSnapshot) -> Unit,
    crossinline completeListener: (String) -> Unit,  // sending download url
    crossinline successListener: (Uri) -> Unit,
    crossinline failureListener: (Exception) -> Unit
) {

    fileReference.putFile(documentUri).let { uploadTask ->

        uploadTask.addOnProgressListener { taskSnapshot -> // UploadTask.TaskSnapshot

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
}
