package com.rohitthebest.manageyourrenters.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.rohitthebest.manageyourrenters.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

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

fun Context.isInternetAvailable() = Functions.isInternetAvailable(this)

fun <T> Context.saveAnyObjectToSharedPreference(
    sharedPrefName: String,
    key: String,
    value: T
) {

    try {
        val sharedPreference =
            this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)
        val edit = sharedPreference.edit()

        val valueToGsonString = if (value is String) {
            value
        } else {
            value.convertToJsonString()
        }

        edit.putString(key, valueToGsonString)
        edit.apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun <T> Context.loadAnyValueFromSharedPreference(
    type: Class<T>,
    sharedPrefName: String,
    key: String
): T? {

    return try {

        val sharedPreference =
            this.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE)

        val valueString = sharedPreference.getString(key, "")

        valueString?.convertJsonToObject(type)

    } catch (e: Exception) {

        e.printStackTrace()
        null
    }
}

fun <T> T.convertToJsonString(): String? {

    return try {

        Gson().toJson(this)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun <T> String.convertJsonToObject(clazz: Class<T>): T? {

    return try {
        Gson().fromJson(this, clazz)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun RecyclerView.changeVisibilityOfFABOnScrolled(fab: FloatingActionButton) {

    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            try {
                if (dy > 0 && fab.visibility == View.VISIBLE) {

                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {

                    fab.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    })
}

fun RecyclerView.changeVisibilityOfViewOnScrolled(view: View) {

    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            try {
                if (dy > 0 && view.visibility == View.VISIBLE) {

                    view.hide()
                } else if (dy < 0 && view.visibility != View.VISIBLE) {

                    view.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    })
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun TextView.changeTextColor(context: Context, color: Int) {

    this.setTextColor(ContextCompat.getColor(context, color))
}

fun TextView.setDateInTextView(timeStamp: Long?, pattern: String = "dd-MM-yyyy") {

    this.text = WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
        timeStamp, pattern
    )

}

fun EditText?.isTextValid(): Boolean {

    return this?.text.toString().isValid()
}

fun String?.isValid(): Boolean {

    return this != null
            && this.trim().isNotEmpty()
            && this.trim().isNotBlank()
}

fun String?.isNotValid() = !this.isValid()

inline fun EditText.onTextChangedListener(
    crossinline onTextChanged: (s: CharSequence?) -> Unit
) {

    this.addTextChangedListener(object : TextWatcher {

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            onTextChanged(s)
        }

        override fun afterTextChanged(s: Editable?) {}
    })

}

inline fun showAlertDialogForDeletion(
    context: Context,
    crossinline positiveButtonListener: (DialogInterface) -> Unit,
    crossinline negativeButtonListener: (DialogInterface) -> Unit,
    message: String = ""
) {

    MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.are_you_sure))
        .setMessage(if (message.isNotValid()) context.getString(R.string.delete_warning_message) else message)
        .setPositiveButton(context.getString(R.string._delete)) { dialogInterface, _ ->

            positiveButtonListener(dialogInterface)
        }
        .setNegativeButton(context.getText(R.string.cancel)) { dialogInterface, _ ->

            negativeButtonListener(dialogInterface)
        }
        .create()
        .show()

}

inline fun View.showSnackbarWithActionAndDismissListener(
    text: String,
    actionText: String,
    crossinline action: (View) -> Unit,
    crossinline dismissListener: () -> Unit
) {

    Snackbar.make(this, text, Snackbar.LENGTH_LONG)
        .setAction(actionText) {

            action(it)
        }
        .addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                dismissListener()
            }
        })
        .show()
}

fun Uri.getFileNameAndSize(contentResolver: ContentResolver): Pair<String, Long>? {

    contentResolver
        .query(
            this, null, null, null, null
        )?.use { cursor ->

            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            cursor.moveToFirst()

            val fileName = cursor.getString(nameIndex)
            val size = cursor.getLong(sizeIndex)

            return Pair(fileName, size)
        }

    return null
}

inline fun SearchView.onTextChanged(
    crossinline onTextChanged: (newText: String?) -> Unit
) {

    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean =
            true

        override fun onQueryTextChange(newText: String?): Boolean {

            onTextChanged(newText)
            return true
        }

    })
}

inline fun SearchView.onTextSubmit(
    crossinline onTextSubmit: (newText: String?) -> Unit
) {

    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {

            onTextSubmit(query)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }

    })
}

suspend inline fun Job?.executeAfterDelay(
    timeMillis: Long = 300,
    crossinline executeMethod: () -> Unit
) {

    try {
        if (this != null && this.isActive) {
            this.cancel()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        delay(timeMillis)
        executeMethod()
    }
}


fun TextView.strikeThrough() {

    val spannableStringBuilder = SpannableStringBuilder(this.text.toString())
    val strikeThroughSpan = StrikethroughSpan()

    spannableStringBuilder.setSpan(
        strikeThroughSpan,
        0,
        this.text.toString().length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    this.text = spannableStringBuilder
}

fun TextView.underline() {

    val spannableStringBuilder = SpannableStringBuilder(this.text.toString())
    val span = UnderlineSpan()

    spannableStringBuilder.setSpan(
        span,
        0,
        this.text.toString().length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    this.text = spannableStringBuilder
}

inline fun Spinner.setListToSpinner(
    context: Context,
    list: List<Any>,
    crossinline position: (Int) -> Unit,
    crossinline selectedValue: (Any) -> Unit
) {


    this.apply {

        adapter = ArrayAdapter(
            context,
            R.layout.support_simple_spinner_dropdown_item,
            list
        )

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                setSelection(position)
                position(position)
                selectedValue(list[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

                setSelection(0)
                position(0)
                selectedValue(list[0])
            }
        }
    }
}

fun View.loadBitmap(): Bitmap {

    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGBA_F16)

    } else {

        Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    }
    val canvas = Canvas(bitmap)

    this.draw(canvas)

    return bitmap
}

fun Bitmap.saveToStorage(context: Context, fileName: String): Uri? {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.jpeg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/ManageYourRenters"
            )
        }

        val uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            resolver.openOutputStream(it).use { fout ->

                try {

                    this.compress(Bitmap.CompressFormat.JPEG, 100, fout!!)
                    fout?.close()

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        return uri

    } else {

        val bytes = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        val path = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            this,
            fileName,
            null
        )

        return Uri.parse(path)
    }
}

fun TextView.applyStyles(text: String, textStyle: String) {

    when {

        text.endsWith("-heading") || (textStyle.isValid() && textStyle == "heading") -> {

            this.changeTextStyle(
                color = R.color.primaryTextColor,
                typeface = Typeface.BOLD,
                textSize = 20f,
                text = text.replace("-heading", "")
            )
        }

        text.endsWith("-critical") || (textStyle.isValid() && textStyle == "critical") -> {

            this.changeTextStyle(
                R.color.color_orange,
                Typeface.BOLD,
                20f,
                text.replace("-critical", "")
            )
        }

        text.startsWith("https") || text.startsWith("http") -> {

            this.changeTextStyle(
                R.color.blue_text_color,
                Typeface.NORMAL,
                16f,
                text
            )
            this.underline()
        }

        else -> {

            if (textStyle.isValid()) {

                val typeface = when {

                    textStyle.contains("B") && textStyle.contains("I") -> {
                        Typeface.BOLD_ITALIC
                    }

                    textStyle.contains("B") -> {
                        Typeface.BOLD
                    }

                    textStyle.contains("I") -> {
                        Typeface.ITALIC
                    }

                    else -> Typeface.NORMAL
                }
                this.changeTextStyle(
                    if (textStyle.contains("B")) R.color.primaryTextColor else R.color.secondaryTextColor,
                    typeface,
                    16f,
                    text
                )

                if (textStyle.contains("U")) {
                    this.underline()
                }

            } else {
                this.changeTextStyle(
                    R.color.secondaryTextColor,
                    Typeface.NORMAL,
                    16f,
                    text
                )
            }
        }
    }
}

fun TextView.changeTextStyle(color: Int, typeface: Int, textSize: Float, text: String) {

    this.apply {

        changeTextColor(
            this.context,
            color
        )
        setTypeface(null, typeface)
        this.textSize = textSize
        this.text = text
    }
}
