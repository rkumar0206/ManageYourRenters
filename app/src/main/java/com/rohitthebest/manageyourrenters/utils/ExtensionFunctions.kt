package com.rohitthebest.manageyourrenters.utils

import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rohitthebest.manageyourrenters.R

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

fun TextView.changeTextColor(context: Context, color: Int) {

    this.setTextColor(ContextCompat.getColor(context, color))
}

fun TextView.setDateInTextView(timeStamp: Long?, pattern: String = "dd-MM-yyyy") {

    this.text = WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
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
            && this.trim() != "null"
}

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
    crossinline negativeButtonListener: (DialogInterface) -> Unit
) {

    MaterialAlertDialogBuilder(context)
        .setTitle("Are you sure?")
        .setMessage(context.getString(R.string.delete_warning_message))
        .setPositiveButton("Delete") { dialogInterface, _ ->

            positiveButtonListener(dialogInterface)
        }
        .setNegativeButton("Cancel") { dialogInterface, _ ->

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

inline fun SearchView.searchText(

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
