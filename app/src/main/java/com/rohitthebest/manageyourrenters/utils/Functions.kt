package com.rohitthebest.manageyourrenters.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.NO_INTERNET_MESSAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Functions {

    companion object {


        private val gson = Gson()
        private val mAuth = Firebase.auth

        private const val TAG = "Functions"
        fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
            try {
                Log.d(TAG, message)
                Toast.makeText(context, message, duration).show()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun isInternetAvailable(context: Context): Boolean {

            return CheckNetworkConnection().isInternetAvailable(context)
        }

        fun showNoInternetMessage(context: Context) {

            showToast(context, NO_INTERNET_MESSAGE)
        }

        fun checkUrl(url: String): String {

            var urll = ""
            try {
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    urll = url
                } else if (url.isNotEmpty()) {
                    urll = "https://www.google.com/search?q=$url"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return urll

        }

/*
        fun openLinkInBrowser(url: String?, context: Context) {

            if (isInternetAvailable(context)) {
                url?.let {

                    try {
                        Log.d(TAG, "Loading Url in default browser.")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkUrl(it)))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        showToast(context, e.message.toString())
                        e.printStackTrace()
                    }
                }
            } else {
                showToast(context, Constants.NO_INTERNET_MESSAGE)
            }
        }
*/

        fun shareAsText(message: String?, subject: String?, context: Context) {

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)
            context.startActivity(Intent.createChooser(intent, "Share Via"))

        }

        fun copyToClipBoard(activity: Activity, text: String) {

            val clipboardManager =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clipData = ClipData.newPlainText("url", text)

            clipboardManager.setPrimaryClip(clipData)

        }

        fun hideKeyBoard(activity: Activity) {

            try{

                GlobalScope.launch {

                    closeKeyboard(activity)
                }

            }catch (e : Exception){

                e.printStackTrace()
            }
        }


        suspend fun closeKeyboard(activity: Activity) {
            try {
                withContext(Dispatchers.IO) {

                    val view = activity.currentFocus

                    if (view != null) {

                        val inputMethodManager =
                            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                    }

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

         fun showKeyboard(activity: Activity,view : View) {
            try {

                val inputMethodManager =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun getUid(): String? {

            return mAuth.currentUser?.uid
        }


        fun View.show() {

            try {
                this.visibility = View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        fun View.hide() {

            try {
                this.visibility = View.GONE

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun View.invisible() {

            try {
                this.visibility = View.INVISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

}