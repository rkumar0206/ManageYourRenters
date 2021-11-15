package com.rohitthebest.manageyourrenters.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.others.Constants.NO_INTERNET_MESSAGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class Functions {

    companion object {

        private val mAuth = Firebase.auth

        private const val TAG = "Functions"
        fun showToast(context: Context, message: Any?, duration: Int = Toast.LENGTH_SHORT) {
            try {
                Log.d(TAG, message.toString())
                Toast.makeText(context, message.toString(), duration).show()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        @JvmName("showToast1")
        fun Context.showToast(message: Any?, duration: Int = Toast.LENGTH_SHORT) {
            try {
                Log.d(TAG, message.toString())
                Toast.makeText(this, message.toString(), duration).show()
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

        /*fun shareAsText(message: String?, subject: String?, context: Context) {

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

        }*/

        fun hideKeyBoard(activity: Activity) {

            try {

                CoroutineScope(Dispatchers.Main).launch {

                    Log.i(TAG, "Function: hideKeyboard")
                    closeKeyboard(activity)
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }

        suspend fun closeKeyboard(activity: Activity) {

            try {
                withContext(Dispatchers.IO) {

                    val view = activity.currentFocus

                    if (view != null) {

                        Log.i(TAG, "Function: closeKeyboard")

                        val inputMethodManager =
                            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                    }

                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun showKeyboard(activity: Activity, view: View) {
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


        fun generateRenterPassword(renterID: String?, mobileNum: String): String {

            val firstFour = renterID?.subSequence(
                (renterID.length / 2),
                renterID.length
            ).toString()

            val lastFour = mobileNum.subSequence(
                (mobileNum.length / 2) + 1,
                mobileNum.length
            ).toString()
            Log.i(TAG, "Generating password -> SUCCESS...")
            return "$firstFour$lastFour#"

        }

        fun saveBooleanToSharedPreference(
            activity: Activity,
            sharedPrefName: String,
            key: String,
            value: Boolean
        ) {

            try {
                val sharedPreferences =
                    activity.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

                val edit = sharedPreferences.edit()

                edit.putBoolean(key, value)

                edit.apply()
            } catch (e: Exception) {
                Log.e(TAG, "saveData: ${e.message}")
            }

        }

        fun loadBooleanFromSharedPreference(
            activity: Activity,
            sharedPrefName: String,
            key: String
        ): Boolean {

            return try {
                val sharedPreferences =
                    activity.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

                sharedPreferences.getBoolean(key, false)

            } catch (e: Exception) {
                Log.e(TAG, "saveData: ${e.message}")
                false
            }

        }


        fun Long.toStringM(radix: Int = 65): String {

            val values = arrayOf(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k",
                "l",
                "m",
                "n",
                "o",
                "p",
                "q",
                "r",
                "s",
                "t",
                "u",
                "v",
                "w",
                "x",
                "y",
                "z",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H",
                "I",
                "J",
                "K",
                "L",
                "M",
                "N",
                "O",
                "P",
                "Q",
                "R",
                "S",
                "T",
                "U",
                "V",
                "W",
                "X",
                "Y",
                "Z",
                "!",
                "@",
                "#",
                "$",
                "%",
                "^",
                "&"
            )
            var str = ""
            var d = this
            var r: Int

            if (radix in 1..69) {

                if (d <= 0) {
                    return d.toString()
                }

                while (d != 0L) {

                    r = (d % radix).toInt()
                    d /= radix
                    str = values[r] + str
                }

                return str
            }

            return d.toString()
        }

        fun generateKey(appendString: String = "", radix: Int = 68): String {

            return "${System.currentTimeMillis().toStringM(radix)}_${
                Random.nextLong(
                    100,
                    9223372036854775
                ).toStringM(radix)
            }$appendString"
        }


        inline fun showCalendarDialog(
            selectedDate: Long,
            crossinline show: () -> FragmentManager,
            crossinline positiveListener: (time: Long) -> Unit,
            isUpcomingDatesValid: Boolean = false
        ) {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(selectedDate)

            if (!isUpcomingDatesValid) {

                val constrainBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()

                datePicker.setCalendarConstraints(constrainBuilder)
            }

            val builder = datePicker.build()

            builder.show(
                show(),
                "datePicker"
            )

            builder.addOnPositiveButtonClickListener {

                positiveListener(it)
            }
        }

        inline fun showDateRangePickerDialog(
            startDate: Long,
            endDate: Long,
            crossinline fragmentManager: () -> FragmentManager,
            crossinline positiveListener: (androidx.core.util.Pair<Long, Long>) -> Unit,
            isUpcomingDatesValid: Boolean = false
        ) {

            val datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(androidx.core.util.Pair(startDate, endDate))
                .setTitleText("Select date range")

            if (!isUpcomingDatesValid) {

                val constrainBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()

                datePicker.setCalendarConstraints(constrainBuilder)
            }

            val builder = datePicker.build()

            builder.show(
                fragmentManager(),
                "date_range_picker"
            )

            builder.addOnPositiveButtonClickListener {

                positiveListener(it)
            }
        }

        fun calculateNumberOfDays(startDate: Long, endDate: Long): Int {

            return ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt()

        }

        fun Context.checkIfPermissionsGranted(permission: String): Boolean {

            return ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }


        private fun checkUrl(url: String): String {

            return try {
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    url
                } else if (url.trim().isNotEmpty()) {
                    "https://www.google.com/search?q=$url"
                } else {
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        private fun openLinkInBrowser(context: Context, url: String) {

            if (isInternetAvailable(context)) {

                Log.d(TAG, "Loading Url in default browser.")

                if (url.isValid()) {

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkUrl(url)))
                    context.startActivity(intent)
                } else {

                    Toast.makeText(context, "Invalid url!!!", Toast.LENGTH_SHORT).show()
                }

            } else {
                showNoInternetMessage(context)
            }
        }

        fun onViewOrDownloadSupportingDocument(
            activity: Activity,
            supportingDoc: SupportingDocument
        ) {


            val title: String
            val message: String
            var positiveBtnText = "Download"

            when (supportingDoc.documentType) {

                DocumentType.PDF -> {
                    title = "Download PDF"
                    message = "PDF name : ${supportingDoc.documentName}"
                }

                DocumentType.IMAGE -> {
                    title = "Download Image"
                    message = "Image name : ${supportingDoc.documentName}"
                }

                else -> {
                    title = "Open url in browser"
                    message = "Description : ${supportingDoc.documentName}" +
                            "\nUrl :  ${supportingDoc.documentUrl} in browser..."
                    positiveBtnText = "Open"
                }
            }

            MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBtnText) { dialog, _ ->

                    if (supportingDoc.documentType != DocumentType.URL) {

                        downloadFileFromUrl(
                            activity,
                            supportingDoc.documentUrl,
                            supportingDoc.documentName
                        )
                    } else {

                        openLinkInBrowser(activity, supportingDoc.documentUrl)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->

                    dialog.dismiss()
                }
                .create()
                .show()

        }

        inline fun setImageToImageViewUsingGlide(
            context: Context,
            imageView: ImageView,
            imageUrl: String?,
            crossinline onLoadFailed: () -> Unit,
            crossinline onResourceReady: () -> Unit
        ) {

            Glide.with(context)
                .load(imageUrl)
                .apply {
                    this.error(R.drawable.ic_outline_error_outline_24)
                    this.placeholder(R.drawable.gradient_blue)
                }
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {

                        onLoadFailed()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {

                        onResourceReady()
                        return false
                    }

                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)

        }

    }

}