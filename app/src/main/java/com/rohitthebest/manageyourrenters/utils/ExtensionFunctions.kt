package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
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

fun EditText.isTextValid(): Boolean {

    return this.text.toString().trim().isNotEmpty()
            && this.text.toString().trim().isNotBlank()
            && this.text.toString().trim() != "null"
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
