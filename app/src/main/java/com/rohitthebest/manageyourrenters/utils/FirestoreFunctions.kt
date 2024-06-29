package com.rohitthebest.manageyourrenters.utils

import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await

suspend fun insertToFireStore(docRef: DocumentReference, data: Any): Boolean {

    return try {

        docRef.set(data)
            .await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}

suspend fun deleteFilesFromFirestore(batch: WriteBatch): Boolean {

    return try {
        batch.commit()
            .await()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}

suspend fun uploadBatchDocumentsToFirestore(batch: WriteBatch): Boolean {

    return try {
        batch.commit()
            .await()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}

suspend fun deleteFileFromFirebaseStorage(mStorageRef: StorageReference?): Boolean {

    return try {
        mStorageRef?.delete()?.await()

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun deleteFromFireStore(docRef: DocumentReference): Boolean {

    return try {

        docRef.delete().await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun updateDocumentOnFireStore(
    docRef: DocumentReference,
    map: java.util.HashMap<String, Any?>
): Boolean {

    return try {

        docRef.update(map)
            .await()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun uploadFilesOnFirestore(batch: WriteBatch): Boolean {

    return try {

        batch.commit().await()

        true
    } catch (e: Exception) {

        e.printStackTrace()
        false
    }
}

suspend inline fun uploadFileUriOnFirebaseStorage(
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