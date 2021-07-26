package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.InterestTimeSchedule
import com.rohitthebest.manageyourrenters.data.InterestType
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.CalculateInterestBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentCalculateInterestBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateNumberOfDays
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showDateRangePickerDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.setDateInTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.pow

private const val TAG = "CalculateInterestBottom"

@AndroidEntryPoint
class CalculateInterestBottomSheetFragment : BottomSheetDialogFragment(),
    RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private var _binding: FragmentCalculateInterestBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: CalculateInterestBottomSheetLayoutBinding

    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private var receivedBorrowerPaymentKey = ""
    private lateinit var receivedBorrowerPayment: BorrowerPayment
    private lateinit var numberOfTimeTypeList: List<String>
    private var selectedNumberOfTimeType = ""
    private var numberOfDays = 0
    private var fromDate: Long = 0L
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

        initListener()
        getMessage()
        setUpNumberOfTimeTypeSpinner()
    }

    private fun setUpNumberOfTimeTypeSpinner() {

        includeBinding.numberOfSpinner.apply {

            adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                numberOfTimeTypeList
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                    setSelection(0)
                    selectedNumberOfTimeType = numberOfTimeTypeList[0]
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    setSelection(position)
                    selectedNumberOfTimeType = numberOfTimeTypeList[position]
                }
            }

        }
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {
                CalculateInterestBottomSheetFragmentArgs.fromBundle(it)
            }

            receivedBorrowerPaymentKey = args?.paymentKey!!

            getBorrowerPayment()
        }
    }

    private fun getBorrowerPayment() {

        borrowerPaymentViewModel.getBorrowerPaymentByKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner, { borrowerPayment ->

                receivedBorrowerPayment = borrowerPayment

                initUI()
            })
    }

    private fun initUI() {

        if (this::receivedBorrowerPayment.isInitialized) {

            val interest = receivedBorrowerPayment.interest!!

            Log.d(TAG, "initUI: interest : $interest")

            if (interest.type == InterestType.SIMPLE_INTEREST) {

                includeBinding.intTypeRG.check(R.id.intSimpleInterestRB)
            } else {

                includeBinding.intTypeRG.check(R.id.intCompoundInterestRB)
            }

            includeBinding.intRateET.setText(interest.ratePercent.toString())
            includeBinding.intPrincipalET.setText(receivedBorrowerPayment.amountTakenOnRent.toString())

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

            if (validateForm()) {

                calculateInterest()
            }

            fromDate = receivedBorrowerPayment.created
            tillDate = System.currentTimeMillis()
            numberOfDays = calculateNumberOfDays(fromDate, tillDate)

            setDateInFromAndTillTextView()
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        if (group?.id == includeBinding.intTimeTypeRG.id) {

            if (checkedId == R.id.numbeOfRB) {

                showNumberOfLinearLayout(true)
            } else {

                showNumberOfLinearLayout(false)
            }

            if (validateForm()) {

                calculateInterest()
            }
        }
    }

    private fun showNumberOfLinearLayout(isVisible: Boolean) {

        includeBinding.intTimeTypeNumberOfLL.isVisible = isVisible
        includeBinding.intTimeTypeRangeCL.isVisible = !isVisible
    }


    private fun initListener() {

        binding.calculateIntToolbar.setNavigationOnClickListener {

            dismiss()
        }

        includeBinding.intTimeTypeRG.setOnCheckedChangeListener(this)

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
                    }
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
                principal * ((1 + rate / 100).pow(timeInYear))
            }

        includeBinding.intCalcInterestTV.text =
            String.format("${receivedBorrowerPayment.currencySymbol} %.3f", interest)

        includeBinding.intCalcTotalTV.text =
            String.format("${receivedBorrowerPayment.currencySymbol} %.3f", (principal + interest))

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