package com.rohitthebest.manageyourrenters.utils

import android.app.Activity
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.NO_INTERNET_MESSAGE
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMI_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMIs
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

private const val TAG = "Functions"

class Functions {

    companion object {

        private val mAuth = Firebase.auth

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


        fun generateRenterOrBorrowerId(name: String, num: String): String {

            var id: String = if (name.length > 4) {

                name.substring(0, 5)
            } else {
                name
            }

            id += if (!num.isValid()) {

                Random.nextLong(80000, 90000).toStringM(36).substring(0, 4)
            } else {

                if (num.length > 4) {

                    num.substring(0, 4)
                } else {
                    num
                }
            }

            id += "_${Random.nextInt(100, 999)}"

            return id
        }

        fun saveBooleanToSharedPreference(
            context: Context,
            sharedPrefName: String,
            key: String,
            value: Boolean
        ) {

            try {
                val sharedPreferences =
                    context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

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

            return "${UUID.randomUUID()}_${
                Random.nextLong(
                    1000,
                    9000
                ).toStringM(radix)
            }$appendString"
        }


        inline fun showCalendarDialog(
            selectedDate: Long,
            crossinline show: () -> FragmentManager,
            crossinline positiveListener: (time: Long) -> Unit,
            isFutureDatesValid: Boolean = false
        ) {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(selectedDate)

            if (!isFutureDatesValid) {

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

        inline fun showDateAndTimePickerDialog(
            context: Context,
            selectedDate: Calendar,
            isFutureDatesValid: Boolean = false,
            minimumDateMilliseconds: Long = 0L,
            crossinline pickedDateListener: (calendar: Calendar) -> Unit
        ) {

            val startYear = selectedDate.get(Calendar.YEAR)
            val startMonth = selectedDate.get(Calendar.MONTH)
            val startDay = selectedDate.get(Calendar.DAY_OF_MONTH)
            val startHour = selectedDate.get(Calendar.HOUR_OF_DAY)
            val startMinute = selectedDate.get(Calendar.MINUTE)

            val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->

                TimePickerDialog(context, { _, hour, minute ->

                    val pickedDateTime = Calendar.getInstance()
                    pickedDateTime.set(year, month, day, hour, minute)

                    pickedDateListener(pickedDateTime)

                }, startHour, startMinute, false).show()

            }

            val datePickerDialog =
                DatePickerDialog(context, dateListener, startYear, startMonth, startDay)

            if (!isFutureDatesValid) {

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            }

            if (minimumDateMilliseconds != 0L) {

                datePickerDialog.datePicker.minDate = minimumDateMilliseconds
            }

            datePickerDialog.show()
        }


        fun calculateNumberOfDays(startDate: Long, endDate: Long): Int {

            return ((endDate - startDate) / (1000 * 60 * 60 * 24)).toInt() + 1

        }

        fun Context.isPermissionGranted(permission: String): Boolean {

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

        fun openLinkInBrowser(context: Context, url: String) {

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
                            "\nUrl :  ${supportingDoc.documentUrl}"
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
            crossinline onResourceReady: () -> Unit,
            placeholder: Int = R.drawable.gradient_blue
        ) {

            Glide.with(context)
                .load(imageUrl)
                .error(R.drawable.ic_outline_error_outline_24)
                .placeholder(placeholder)
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

        fun copyToClipBoard(activity: Activity, text: String, label: String = "text") {

            val clipboardManager =
                activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clipData = ClipData.newPlainText(label, text)

            clipboardManager.setPrimaryClip(clipData)

            showToast(activity, "copied")
        }

        fun showMobileNumberOptionMenu(
            activity: Activity,
            view: View,
            mobileNumber: String
        ) {

            val popupMenu = PopupMenu(activity, view)

            popupMenu.menuInflater.inflate(R.menu.mobile_number_option_menu, popupMenu.menu)

            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {

                return@setOnMenuItemClickListener when (it.itemId) {

                    R.id.menu_mobile_copy -> {

                        copyToClipBoard(activity, mobileNumber, "mobile number")
                        true
                    }

                    R.id.menu_mobile_call -> {

                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobileNumber"))
                        activity.startActivity(intent)
                        true
                    }

                    else -> {
                        false
                    }
                }
            }

        }

        fun getMonthList(context: Context): List<String> {

            return context.resources.getStringArray(R.array.months).toList()
        }

        inline fun showCustomDateRangeOptionMenu(
            activity: Activity,
            view: View,
            crossinline onMenuItemClicked: (CustomDateRange) -> Unit
        ) {

            val popupMenu = PopupMenu(activity, view)

            popupMenu.menuInflater.inflate(R.menu.custom_expense_date_range_menu, popupMenu.menu)

            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {

                return@setOnMenuItemClickListener when (it.itemId) {

                    R.id.menu_date_range_this_month -> {

                        onMenuItemClicked(CustomDateRange.THIS_MONTH)
                        true
                    }

                    R.id.menu_date_range_this_week -> {

                        onMenuItemClicked(CustomDateRange.THIS_WEEK)
                        true
                    }

                    R.id.menu_date_range_previous_month -> {

                        onMenuItemClicked(CustomDateRange.PREVIOUS_MONTH)
                        true
                    }

                    R.id.menu_date_range_previous_week -> {

                        onMenuItemClicked(CustomDateRange.PREVIOUS_WEEK)
                        true
                    }

                    R.id.menu_date_range_last_30_days -> {
                        onMenuItemClicked(CustomDateRange.LAST_30_DAYS)
                        true
                    }

                    R.id.menu_date_range_last_7_days -> {
                        onMenuItemClicked(CustomDateRange.LAST_7_DAYS)
                        true
                    }

                    R.id.menu_date_range_last_365_days -> {
                        onMenuItemClicked(CustomDateRange.LAST_365_DAYS)
                        true
                    }

                    R.id.menu_date_range_all_time -> {
                        onMenuItemClicked(CustomDateRange.ALL_TIME)
                        true
                    }

                    R.id.menu_date_range_custom_range -> {
                        onMenuItemClicked(CustomDateRange.CUSTOM_DATE_RANGE)
                        true
                    }

                    else -> false
                }
            }
        }

        fun getMillisecondsOfStartAndEndUsingConstants(
            customDateRange: CustomDateRange
        ): Pair<Long, Long> {

            val workingWithDateAndTime = WorkingWithDateAndTime
            val timeInMillis = System.currentTimeMillis()

            when (customDateRange) {

                CustomDateRange.THIS_MONTH -> {

                    return workingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonth(
                        timeInMillis
                    )
                }

                CustomDateRange.PREVIOUS_MONTH -> {

                    val cal =
                        workingWithDateAndTime.convertMillisecondsToCalendarInstance(timeInMillis)

                    cal.add(Calendar.MONTH, -1)

                    return workingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonth(cal.timeInMillis)
                }

                CustomDateRange.THIS_WEEK -> {

                    return workingWithDateAndTime.getMillisecondsOfStartAndEndOfWeek(timeInMillis)
                }

                CustomDateRange.PREVIOUS_WEEK -> {

                    val cal =
                        workingWithDateAndTime.convertMillisecondsToCalendarInstance(timeInMillis)

                    Log.d(
                        TAG, "getMillisecondsOfStartAndEndUsingConstants: Day of week : "
                                + cal.get(Calendar.DAY_OF_WEEK)
                    )

                    when (cal.get(Calendar.DAY_OF_WEEK)) {

                        1 -> cal.add(Calendar.DATE, -7)
                        2 -> cal.add(Calendar.DATE, -1)
                        3 -> cal.add(Calendar.DATE, -2)
                        4 -> cal.add(Calendar.DATE, -3)
                        5 -> cal.add(Calendar.DATE, -4)
                        6 -> cal.add(Calendar.DATE, -5)
                        7 -> cal.add(Calendar.DATE, -6)
                    }

                    return workingWithDateAndTime.getMillisecondsOfStartAndEndOfWeek(cal.timeInMillis)
                }

                else -> {

                    return Pair(timeInMillis, timeInMillis)
                }
            }
        }

        fun getPairOfDateInMillisInStringInDateString(
            dates: Pair<Long, Long>,
            pattern: String = "dd-MM-yyyy"
        ): String {

            val workingWithDateAndTime = WorkingWithDateAndTime

            return "${
                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    dates.first, pattern
                )
            } - ${
                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    dates.second,
                    pattern
                )
            }"
        }


        fun getBackgroundColor(color: Int? = null): Int {

            val h: Float
            var s: Float
            var v: Float

            val hsv = FloatArray(3)

            if (color != null) {

                Color.colorToHSV(color, hsv)

                h = hsv[0]
                s = hsv[1]
                v = hsv[2]

                if (s <= 0.1 && v >= 0.9) {

                    s = 0.1f
                    v = 0.9f
                }
            } else {

                h = Random.nextFloat() * 360
                s = 1f
                v = 1f
            }

            return Color.HSVToColor(30, floatArrayOf(h, s, v))
        }

        suspend fun saveBitmapToCacheDirectoryAndShare(activity: Activity, bitmap: Bitmap?) {

            withContext(Dispatchers.IO) {
                try {

                    val cachePath = File(activity.cacheDir, "images")
                    cachePath.mkdirs()
                    val fos =
                        FileOutputStream("$cachePath/image.png") //overwrites the image everytime
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()

                    //sharing the image
                    shareCachedImage(activity)

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        }

        private fun shareCachedImage(activity: Activity) {

            val imagePath = File(activity.cacheDir, "images")
            val newFile = File(imagePath, "image.png")

            val contentUri = FileProvider.getUriForFile(
                activity,
                Constants.FILE_PROVIDER_AUTHORITY,
                newFile
            )

            if (contentUri != null) {

                shareUri(
                    activity,
                    contentUri
                )
            }
        }

        fun shareUri(activity: Activity, contentUri: Uri) {

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, activity.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            activity.startActivity(Intent.createChooser(shareIntent, "Share Via"))

        }

        /**
         * @param interestCalculatorFields
         * @return Pair object, first : interest, second : amount
         */
        fun calculateInterestAndAmount(interestCalculatorFields: InterestCalculatorFields): Pair<Double, Double> {

            val principal = interestCalculatorFields.principalAmount
            val rate = interestCalculatorFields.interest.ratePercent
            val timeSchedule = interestCalculatorFields.interest.timeSchedule
            val forTime = interestCalculatorFields.forTime   // in days
            val interestType = interestCalculatorFields.interest.type

            val time: Int = when (timeSchedule) {

                InterestTimeSchedule.ANNUALLY -> forTime / 365

                InterestTimeSchedule.MONTHLY -> forTime / 30

                InterestTimeSchedule.DAILY -> forTime
            }

            val interest = if (interestType == InterestType.SIMPLE_INTEREST) {

                (principal * rate * time) / 100
            } else {

                // compound interest
                (principal * ((1 + rate / 100).pow(time))) - principal
            }

            val amount = principal + interest

            Log.d(
                TAG, "calculateInterestAndAmount: " +
                        "\nprincipal : $principal" +
                        "\nrate : $rate" +
                        "\ntimeSchedule : $timeSchedule" +
                        "\nforTime : $forTime" +
                        "\ntime : $time" +
                        "\ninterestType : $interestType" +
                        "\ninterest : $interest" +
                        "\nAmount : $amount"
            )


            return Pair(interest, amount)
        }

        fun getPendingIntentForForegroundServiceNotification(
            context: Context,
            modelCollectionName: String
        ): PendingIntent {

            return Intent(context, HomeActivity::class.java).let { notificationIntent ->

                notificationIntent.action = when (modelCollectionName) {

                    BORROWERS -> Constants.SHORTCUT_BORROWERS
                    RENTERS -> Constants.SHORTCUT_HOUSE_RENTERS
                    BORROWER_PAYMENTS -> Constants.SHORTCUT_BORROWERS
                    RENTER_PAYMENTS -> Constants.SHORTCUT_HOUSE_RENTERS
                    EMIs -> Constants.SHORTCUT_EMI
                    EMI_PAYMENTS -> Constants.SHORTCUT_EMI
                    EXPENSE_CATEGORIES -> Constants.SHORTCUT_EXPENSE
                    EXPENSES -> Constants.SHORTCUT_EXPENSE
                    MONTHLY_PAYMENT_CATEGORIES -> Constants.SHORTCUT_MONTHLY_PAYMENTS
                    MONTHLY_PAYMENTS -> Constants.SHORTCUT_MONTHLY_PAYMENTS

                    else -> ""
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

                    PendingIntent.getActivity(
                        context,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                } else {

                    PendingIntent.getActivity(
                        context, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }

            }
        }

        fun getAppropriateBudgetSuggestionOrMessage(
            context: Context,
            progressInPercent: Int,
            budgetExpense: Double,
            budgetLimit: Double
        ): String {

            val message: List<String>;

            when {

                (progressInPercent in 0..35) -> {
                    message =
                        context.resources.getStringArray(R.array.budget_start_messages).toList()
                }

                (progressInPercent in 36..68) -> {
                    message =
                        context.resources.getStringArray(R.array.budget_middle_messages).toList()
                }

                else -> {
                    message = if (budgetExpense > budgetLimit) {
                        context.resources.getStringArray(R.array.budget_overspent_messages).toList()
                    } else if (budgetExpense == budgetLimit) {
                        context.resources.getStringArray(R.array.budget_reachedLimit_messages)
                            .toList()
                    } else {
                        context.resources.getStringArray(R.array.budget_end_messages).toList()
                    }
                }
            }

            return message[Random.nextInt(0, message.size - 1)]
        }
    }
}