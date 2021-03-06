package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.services.DeleteAllDocumentsService
import com.rohitthebest.manageyourrenters.services.DeleteService
import com.rohitthebest.manageyourrenters.services.UpdateService
import com.rohitthebest.manageyourrenters.services.UploadService

import kotlin.random.Random

class FirebaseServiceHelper {

    companion object {

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

    }
}