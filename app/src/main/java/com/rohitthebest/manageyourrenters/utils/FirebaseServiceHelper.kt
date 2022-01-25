package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DELETE_FILE_FROM_FIREBASE_KEY
import com.rohitthebest.manageyourrenters.others.Constants.FILE_NAME_KEY
import com.rohitthebest.manageyourrenters.others.Constants.FILE_URI_KEY
import com.rohitthebest.manageyourrenters.others.Constants.UPLOAD_DATA_KEY
import com.rohitthebest.manageyourrenters.services.*
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

fun uploadDocumentToFireStore(
    context: Context,
    collection: String,
    documentKey: String
) {

    val foregroundService = Intent(context, UploadService::class.java)

    foregroundService.putExtra(
        COLLECTION_KEY,
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

fun updateDocumentOnFireStore(
    context: Context,
    map: HashMap<String, Any?>?,
    collectionKey: String,
    documentKey: String
) {

    val foregroundService = Intent(context, UpdateService::class.java)

    foregroundService.putExtra(
        COLLECTION_KEY,
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
        COLLECTION_KEY,
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

fun uploadListOfDataToFireStore(
    context: Context,
    collection: String,
    uploadData: String
) {

    val foregroundService = Intent(context, UploadDocumentListToFireStoreService::class.java)

    foregroundService.putExtra(
        COLLECTION_KEY,
        collection
    )

    foregroundService.putExtra(
        Constants.RANDOM_ID_KEY,
        Random.nextInt(1000, 9999)
    )

    foregroundService.putExtra(
        UPLOAD_DATA_KEY,
        uploadData
    )

    ContextCompat.startForegroundService(context, foregroundService)

}

fun deleteAllDocumentsUsingKeyFromFirestore(
    context: Context,
    collection: String,
    keyList: String
) {

    val foregroundService = Intent(context, DeleteAllDocumentsService::class.java)

    foregroundService.putExtra(
        COLLECTION_KEY,
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

fun deleteFileFromFirebaseStorage(context: Context, documentUrl: String) {

    val foregroundService = Intent(context, DeleteFileFromFirebaseStorageService::class.java)

    foregroundService.putExtra(DELETE_FILE_FROM_FIREBASE_KEY, documentUrl)

    foregroundService.putExtra(
        Constants.RANDOM_ID_KEY,
        Random.nextInt(1000, 9999)
    )

    ContextCompat.startForegroundService(context, foregroundService)
}


fun uploadFileToFirebaseStorage(
    context: Context,
    fileInfo: Pair<Uri, String>,  // pass fileUri and fileName
    uploadDataInfo: Pair<String, String>, // pass upload data and collection
) {

    val foregroundServiceIntent = Intent(context, UploadFileToFirebaseStorageService::class.java)

    foregroundServiceIntent.putExtra(FILE_URI_KEY, fileInfo.first.toString())
    foregroundServiceIntent.putExtra(FILE_NAME_KEY, fileInfo.second)
    foregroundServiceIntent.putExtra(UPLOAD_DATA_KEY, uploadDataInfo.first)
    foregroundServiceIntent.putExtra(COLLECTION_KEY, uploadDataInfo.second)

    ContextCompat.startForegroundService(context, foregroundServiceIntent)
}

suspend fun getDataFromFireStore(
    collection: String,
    uid: String,
    failureListener: (Exception) -> Unit
): QuerySnapshot? {

    return try {

        FirebaseFirestore.getInstance()
            .collection(collection)
            .whereEqualTo("uid", uid)
            .get()
            .addOnFailureListener {
                failureListener(it)
            }
            .await()

    } catch (e: java.lang.Exception) {

        e.printStackTrace()
        null
    }
}
