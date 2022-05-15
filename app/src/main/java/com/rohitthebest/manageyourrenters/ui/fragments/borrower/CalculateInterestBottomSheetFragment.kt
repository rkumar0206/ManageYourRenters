package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.Interest
import com.rohitthebest.manageyourrenters.data.InterestCalculatorFields
import com.rohitthebest.manageyourrenters.data.InterestTimeSchedule
import com.rohitthebest.manageyourrenters.data.InterestType
import com.rohitthebest.manageyourrenters.databinding.CalculateInterestBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentCalculateInterestBinding
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateInterestAndAmount
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateNumberOfDays
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showDateRangePickerDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalculateInterestBottomSheetFragment : BottomSheetDialogFragment(),
    RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private var _binding: FragmentCalculateInterestBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: CalculateInterestBottomSheetLayoutBinding

    private var receivedInterestCalculatorField: InterestCalculatorFields? = null
    private var selectedInterestTimeSchedule: InterestTimeSchedule = InterestTimeSchedule.ANNUALLY
    private var numberOfDays = 0
    private var fromDate = 0L
    private var tillDate = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_calculate_interest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCalculateInterestBinding.bind(view)
        includeBinding = binding.includeLayout

        setUpTimeScheduleSpinner()

        getMessage()

        lifecycleScope.launch {
            delay(150)
            initListener()
            textWatchers()
        }
    }

    private fun textWatchers() {

        includeBinding.intRateET.onTextChangedListener { s ->
            if (s?.isNotBlank()!!) {
                if (validateForm()) calculateInterest()
            }
        }

        includeBinding.intPrincipalET.onTextChangedListener { s ->

            if (s?.isNotBlank()!!) {
                if (validateForm()) calculateInterest()
            }
        }
    }

    private fun setUpTimeScheduleSpinner() {

        val interestTimeScheduleList =
            resources.getStringArray(R.array.interest_time_schedule).toList()

        includeBinding.interestTimeSheduleSpinner.setListToSpinner(
            requireContext(),
            interestTimeScheduleList,
            { position ->
                selectedInterestTimeSchedule = when (position) {

                    0 -> InterestTimeSchedule.ANNUALLY
                    1 -> InterestTimeSchedule.MONTHLY
                    2 -> InterestTimeSchedule.DAILY
                    else -> InterestTimeSchedule.ANNUALLY
                }

                if (validateForm()) calculateInterest()
            },
            {}
        )
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {
                CalculateInterestBottomSheetFragmentArgs.fromBundle(it)
            }

            receivedInterestCalculatorField =
                args?.interestCalcualatorFields!!.convertJsonToObject(InterestCalculatorFields::class.java)

            if (receivedInterestCalculatorField == null) {

                dismiss()
            }

            initUI()
        }
    }

    private fun initUI() {

        if (receivedInterestCalculatorField != null) {

            val interest = receivedInterestCalculatorField?.interest!!

            if (interest.type == InterestType.SIMPLE_INTEREST) {

                includeBinding.interestTypeRG.check(R.id.intSimpleInterestRB)
            } else {

                includeBinding.interestTypeRG.check(R.id.intCompoundInterestRB)
            }

            includeBinding.intRateET.setText(interest.ratePercent.toString())
            includeBinding.intPrincipalET.setText(receivedInterestCalculatorField?.principalAmount.toString())

            when (interest.timeSchedule) {

                InterestTimeSchedule.ANNUALLY -> {

                    selectedInterestTimeSchedule = InterestTimeSchedule.ANNUALLY // Annually
                    includeBinding.interestTimeSheduleSpinner.setSelection(0)
                }
                InterestTimeSchedule.MONTHLY -> {

                    selectedInterestTimeSchedule = InterestTimeSchedule.MONTHLY // Monthly
                    includeBinding.interestTimeSheduleSpinner.setSelection(1)
                }
                InterestTimeSchedule.DAILY -> {

                    selectedInterestTimeSchedule = InterestTimeSchedule.DAILY // Daily
                    includeBinding.interestTimeSheduleSpinner.setSelection(2)
                }
            }

            fromDate = receivedInterestCalculatorField?.startDate!!
            tillDate = System.currentTimeMillis()
            numberOfDays = calculateNumberOfDays(fromDate, tillDate)

            setDateInFromAndTillTextView()

            if (validateForm()) {

                calculateInterest()
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (group?.id) {

            includeBinding.interestTypeRG.id -> {

                if (validateForm()) calculateInterest()
            }
        }

    }

    private fun initListener() {

        binding.calculateIntToolbar.setNavigationOnClickListener {

            dismiss()
        }

        includeBinding.interestTypeRG.setOnCheckedChangeListener(this)

        includeBinding.interestCalculateBtn.setOnClickListener(this)
        includeBinding.intSelectDateRangeIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            includeBinding.interestCalculateBtn.id -> {

                if (validateForm()) {

                    calculateInterest()
                }
            }

            includeBinding.intSelectDateRangeIB.id -> {

                showDateRangePickerDialog(
                    fromDate,
                    tillDate,
                    {
                        requireActivity().supportFragmentManager
                    },
                    {
                        fromDate = it.first
                        tillDate = it.second
                        numberOfDays = calculateNumberOfDays(fromDate, tillDate)

                        setDateInFromAndTillTextView()

                        if (validateForm()) calculateInterest()
                    },
                    true
                )
            }
        }

        hideKeyBoard(requireActivity())
    }

    @SuppressLint("SetTextI18n")
    private fun calculateInterest() {

        val interestCalculatorFields = InterestCalculatorFields(
            0L,
            includeBinding.intPrincipalET.text.toString().toDouble(),
            Interest(
                if (includeBinding.interestTypeRG.checkedRadioButtonId == includeBinding.intSimpleInterestRB.id) {

                    InterestType.SIMPLE_INTEREST
                } else {
                    InterestType.COMPOUND_INTEREST
                },
                includeBinding.intRateET.text.toString().toDouble(),
                selectedInterestTimeSchedule
            ),
            numberOfDays
        )

        val interestAndAmount = calculateInterestAndAmount(interestCalculatorFields)

        includeBinding.intCalcInterestTV.text = interestAndAmount.first.format(3)
        includeBinding.intCalcTotalTV.text = interestAndAmount.second.format(3)
    }

    private fun validateForm(): Boolean {

        if (!includeBinding.intRateET.isTextValid()) {

            includeBinding.intRateET.requestFocus()
            showToast(requireContext(), "Please enter rate percent")
            return false
        }

        if (!includeBinding.intPrincipalET.isTextValid()) {

            includeBinding.intPrincipalET.requestFocus()
            showToast(requireContext(), "Please enter principal amount")
            return false
        }
        return true
    }

    private fun setDateInFromAndTillTextView() {

        includeBinding.intFromTV.setDateInTextView(
            fromDate
        )
        includeBinding.intTillTV.setDateInTextView(
            tillDate
        )

        includeBinding.numberOfDaysTV.text =
            getString(R.string.number_of_days_with_value, numberOfDays.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}