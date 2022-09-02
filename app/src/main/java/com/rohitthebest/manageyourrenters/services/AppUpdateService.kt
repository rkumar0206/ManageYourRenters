package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.rohitthebest.manageyourrenters.data.AppUpdate
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBooleanToSharedPreference
import com.rohitthebest.manageyourrenters.utils.getDocumentFromFirestore
import com.rohitthebest.manageyourrenters.utils.saveAnyObjectToSharedPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AppUpdateService"

class AppUpdateService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        CoroutineScope(Dispatchers.IO).launch {

            getDocumentFromFirestore(
                Constants.APP_UPDATE_FIRESTORE_COLLECTION_NAME,
                Constants.APP_UPDATE_FIRESTORE_DOCUMENT_KEY,
                { appUpdateDocSnapshot ->

                    if (appUpdateDocSnapshot != null) {

                        val appUpdate = appUpdateDocSnapshot.toObject(AppUpdate::class.java)

                        applicationContext.saveAnyObjectToSharedPreference(
                            Constants.APP_UPDATE_SHARED_PREF_NAME,
                            Constants.APP_UPDATE_SHARED_PREF_KEY,
                            appUpdate
                        )

                        saveBooleanToSharedPreference(
                            applicationContext,
                            Constants.CHECKED_FOR_APP_UPDATE_SHARED_PREF_NAME,
                            Constants.CHECKED_FOR_APP_UPDATE_SHARED_PREF_KEY,
                            true
                        )

                        Log.d(TAG, "onStartCommand: Saved AppUpdate to shared pref : $appUpdate")
                    }

                },
                { failureException ->

                    failureException.printStackTrace()
                }
            )
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}