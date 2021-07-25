package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.Interest

fun showInterestBottomSheet(
    context: Context,
    interest: Interest,
    principal: Double = 0.0,
    fromDate: Long,
    tillDate: Long
) {

    MaterialDialog(context, BottomSheet())
        .show {

            customView(
                R.layout.calculate_interest_bottom_sheet_layout,
                scrollable = true
            )

            val interestTypeRG = getCustomView().findViewById<RadioGroup>(R.id.intTypeRG)
            val simpleInterestRB =
                getCustomView().findViewById<RadioButton>(R.id.intSimpleInterestRB)
            val compoundInterestRB =
                getCustomView().findViewById<RadioButton>(R.id.intCompoundInterestRB)
            val interestRateET = getCustomView().findViewById<EditText>(R.id.intRateET)
            val interestPrincipalET = getCustomView().findViewById<EditText>(R.id.intPrincipalET)

            interestRateET.setText(interest.ratePercent.toString())
            interestPrincipalET.setText(principal.toString())
        }
}