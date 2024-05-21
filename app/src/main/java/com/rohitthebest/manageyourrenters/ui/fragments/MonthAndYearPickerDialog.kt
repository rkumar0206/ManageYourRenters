package com.rohitthebest.manageyourrenters.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.DialogMonthAndYearBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonthAndYearPickerDialog : DialogFragment(R.layout.dialog_month_and_year) {

    private var _binding: DialogMonthAndYearBinding? = null
    private val binding get() = _binding!!

    private var monthList: List<String> = emptyList()
    private var mListener: OnMonthAndYearDialogDismissListener? = null

    private var selectedMonth = 0
    private var selectedYear = 2023
    private var minimumYear = 2000
    private var maximumYear = 2099

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = DialogMonthAndYearBinding.bind(view)

        monthList = resources.getStringArray(R.array.months).toList()

        selectedMonth = WorkingWithDateAndTime.getCurrentMonth()
        selectedYear = WorkingWithDateAndTime.getCurrentYear()
        maximumYear = WorkingWithDateAndTime.getCurrentYear()

        initListeners()
        initUI()
        getMessage()
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                arguments?.let { bundle ->

                    selectedMonth = bundle.getInt(
                        Constants.MONTH_YEAR_PICKER_MONTH_KEY,
                        WorkingWithDateAndTime.getCurrentMonth()
                    )
                    selectedYear = bundle.getInt(
                        Constants.MONTH_YEAR_PICKER_YEAR_KEY,
                        WorkingWithDateAndTime.getCurrentYear()
                    )

                    minimumYear = bundle.getInt(
                        Constants.MONTH_YEAR_PICKER_MIN_YEAR_KEY,
                        2000
                    )

                    maximumYear = bundle.getInt(
                        Constants.MONTH_YEAR_PICKER_MAX_YEAR_KEY,
                        WorkingWithDateAndTime.getCurrentYear()
                    )

                    initUI()
                }
            }
        } catch (e: Exception) {
            showToast(requireContext(), getString(R.string.something_went_wrong))
            dismissDialog(false)
        }
    }

    private fun initUI() {

        binding.monthPicker.minValue = 0
        binding.monthPicker.maxValue = 11
        binding.monthPicker.displayedValues = monthList.toTypedArray()

        binding.yearPicker.minValue = minimumYear
        binding.yearPicker.maxValue = maximumYear

        binding.monthPicker.value = selectedMonth
        binding.yearPicker.value = selectedYear
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {

            dismissDialog(false)
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).apply {
            this.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_check_24)

            this.setOnMenuItemClickListener {

                selectedMonth = binding.monthPicker.value
                selectedYear = binding.yearPicker.value

                dismissDialog(true, selectedMonth, selectedYear)

                false
            }
        }

    }

    private fun dismissDialog(isMonthAndYearSelected: Boolean, month: Int = 0, year: Int = 0) {

        if (mListener != null) {
            mListener!!.onMonthAndYearDialogDismissed(isMonthAndYearSelected, month, year)
        }

        dismiss()
    }

    interface OnMonthAndYearDialogDismissListener {

        fun onMonthAndYearDialogDismissed(
            isMonthAndYearSelected: Boolean,
            selectedMonth: Int,
            selectedYear: Int
        )
    }

    fun setOnMonthAndYearDialogDismissListener(listener: OnMonthAndYearDialogDismissListener) {

        mListener = listener
    }


    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): MonthAndYearPickerDialog {
            val fragment = MonthAndYearPickerDialog()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        _binding = null
    }

}