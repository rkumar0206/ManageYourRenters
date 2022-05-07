package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.InterestCalculatorFields
import com.rohitthebest.manageyourrenters.data.InterestTimeSchedule
import com.rohitthebest.manageyourrenters.data.InterestType
import com.rohitthebest.manageyourrenters.databinding.CalculateInterestBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentCalculateInterestBinding
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateNumberOfDays
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showDateRangePickerDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

private const val TAG = "CalculateInterestBottom"

@AndroidEntryPoint
class CalculateInterestBottomSheetFragment : BottomSheetDialogFragment(),
    RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private var _binding: FragmentCalculateInterestBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: CalculateInterestBottomSheetLayoutBinding

    private var receivedInterestCalculatorField: InterestCalculatorFields? = null
    private lateinit var numberOfTimeTypeList: List<String>
    private var selectedNumberOfTimeType = "days"
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

        numberOfTimeTypeList = resources.getStringArray(R.array.time).asList()

        setUpNumberOfTimeTypeSpinner()

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

        includeBinding.numberOfET.onTextChangedListener { s ->
            if (s?.isNotBlank()!!) {
                if (validateForm()) calculateInterest()
            }
        }
    }

    private fun setUpNumberOfTimeTypeSpinner() {

        includeBinding.numberOfSpinner.setListToSpinner(
            requireContext(),
            numberOfTimeTypeList,
            { position ->

                Log.d(TAG, "setUpNumberOfTimeTypeSpinner: ")

                selectedNumberOfTimeType = numberOfTimeTypeList[position]
                if (validateForm()) {
                    calculateInterest()
                }
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

            Log.d(TAG, "initUI: interest : $interest")

            if (interest.type == InterestType.SIMPLE_INTEREST) {

                includeBinding.intTypeRG.check(R.id.intSimpleInterestRB)
            } else {

                includeBinding.intTypeRG.check(R.id.intCompoundInterestRB)
            }

            includeBinding.intRateET.setText(interest.ratePercent.toString())
            includeBinding.intPrincipalET.setText(receivedInterestCalculatorField?.principalAmount.toString())

            when (interest.timeSchedule) {

                InterestTimeSchedule.DAILY -> {

                    if (includeBinding.intTimeTypeRG.checkedRadioButtonId == R.id.numbeOfRB) {

                        includeBinding.numberOfET.setText("7")
                        includeBinding.numberOfSpinner.setSelection(0)
                        selectedNumberOfTimeType = numberOfTimeTypeList[0]
                    }
                }

                InterestTimeSchedule.MONTHLY -> {

                    if (includeBinding.intTimeTypeRG.checkedRadioButtonId == R.id.numbeOfRB) {

                        includeBinding.numberOfET.setText("1")
                        includeBinding.numberOfSpinner.setSelection(1)
                        selectedNumberOfTimeType = numberOfTimeTypeList[1]
                    }

                }

                InterestTimeSchedule.ANNUALLY -> {

                    if (includeBinding.intTimeTypeRG.checkedRadioButtonId == R.id.numbeOfRB) {

                        includeBinding.numberOfET.setText("1")
                        includeBinding.numberOfSpinner.setSelection(2)
                        selectedNumberOfTimeType = numberOfTimeTypeList[2]
                    }

                }
            }

            Log.d(TAG, "initUI: timeType : $selectedNumberOfTimeType")

            if (validateForm()) {

                calculateInterest()
            }

            fromDate = receivedInterestCalculatorField?.startDate!!
            tillDate = System.currentTimeMillis()
            numberOfDays = calculateNumberOfDays(fromDate, tillDate)

            setDateInFromAndTillTextView()
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (group?.id) {

            includeBinding.intTimeTypeRG.id -> {
                if (checkedId == R.id.numbeOfRB) {

                    showNumberOfLinearLayout(true)
                } else {

                    showNumberOfLinearLayout(false)
                }
            }

            includeBinding.intTypeRG.id -> {

                if (validateForm()) calculateInterest()
            }
        }

    }

    private fun showNumberOfLinearLayout(isVisible: Boolean) {

        includeBinding.intTimeTypeNumberOfLL.isVisible = isVisible
        includeBinding.intTimeTypeRangeCL.isVisible = !isVisible

        if (validateForm()) calculateInterest()
    }


    private fun initListener() {

        binding.calculateIntToolbar.setNavigationOnClickListener {

            dismiss()
        }

        includeBinding.intTimeTypeRG.setOnCheckedChangeListener(this)
        includeBinding.intTypeRG.setOnCheckedChangeListener(this)

        includeBinding.intCalculateBtn.setOnClickListener(this)
        includeBinding.intSelectDateRangeIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            includeBinding.intCalculateBtn.id -> {

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

        val rate = includeBinding.intRateET.text.toString().toDouble()
        val principal = includeBinding.intPrincipalET.text.toString().toDouble()
        var numberOfDays = 0

        if (includeBinding.intTimeTypeRG.checkedRadioButtonId == includeBinding.numbeOfRB.id) {

            val n = includeBinding.numberOfET.text.toString().toInt()

            Log.d(TAG, "calculateInterest: n = : $n")
            Log.d(TAG, "calculateInterest: selectedNumberOfType : $selectedNumberOfTimeType")

            when (selectedNumberOfTimeType) {

                numberOfTimeTypeList[0] -> {
                    numberOfDays = n
                }

                numberOfTimeTypeList[1] -> {

                    numberOfDays = n * 30
                }

                numberOfTimeTypeList[2] -> {

                    numberOfDays = n * 365
                }
            }
        } else {

            numberOfDays = this.numberOfDays
        }

        val timeInYear = (numberOfDays.toDouble() / 365.0)

        val interest: Double =
            if (includeBinding.intTypeRG.checkedRadioButtonId == includeBinding.intSimpleInterestRB.id) {

                // Simple Interest
                (principal * rate * timeInYear) / 100

            } else {

                //compound interest
                (principal * ((1 + rate / 100).pow(timeInYear))) - principal
            }

        includeBinding.intCalcInterestTV.text = interest.format(3)

        includeBinding.intCalcTotalTV.text = (principal + interest).format(3)

        Log.d(
            TAG,
            "calculateInterest: \nRate : $rate" +
                    "\nnumberOfDays : $numberOfDays" +
                    "\nprincipal : $principal" +
                    "\ntimeINYear : $timeInYear" +
                    "\ninterest : $interest" +
                    "\ntotal : ${(principal + interest)}"
        )
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

        if (includeBinding.intTimeTypeRG.checkedRadioButtonId == includeBinding.numbeOfRB.id) {

            if (!includeBinding.numberOfET.isTextValid()) {

                includeBinding.numberOfET.requestFocus()
                showToast(requireContext(), "Please enter the number of $selectedNumberOfTimeType")
                return false
            }

            if (includeBinding.numberOfET.text.toString().trim().toInt() == 0) {

                includeBinding.numberOfET.requestFocus()
                showToast(requireContext(), "number of $selectedNumberOfTimeType cannot be zero(0)")
                return false
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}