package com.rohitthebest.manageyourrenters.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

            try {

                CoroutineScope(Dispatchers.Main).launch {

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


        fun Long.toStringM(radix: Int = 0): String {

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

        fun generateKey(appendString: String = "", radix: Int = 69): String {

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
            crossinline positiveListener: (time: Long) -> Unit
        ) {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(selectedDate)

            val constrainBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build()

            datePicker.setCalendarConstraints(constrainBuilder)

            val builder = datePicker.build()

            builder.show(
                show(),
                "datePicker"
            )

            //builder.show(requireActivity().supportFragmentManager, "datePicker")

            builder.addOnPositiveButtonClickListener {

                positiveListener(it)
            }
        }

    }

}