package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.AddBorrowerPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddBorrowerPaymentFragm"

@AndroidEntryPoint
class AddBorrowerPaymentFragment : Fragment(R.layout.fragment_add_borrower_payment),
    CompoundButton.OnCheckedChangeListener {

    private var _binding: FragmentAddBorrowerPaymentBinding? = null
    private val binding get() = _binding!!

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private var receivedBorrower: Borrower? = null
    private var receivedBorrowerKey: String = ""
    private lateinit var includeBinding: AddBorrowerPaymentLayoutBinding
    private lateinit var currencySymbols: List<String>
    private var selectedCurrencySymbol: String = ""

    private var selectedDate: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBorrowerPaymentBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()

        //List of currency symbols of different places
        currencySymbols = resources.getStringArray(R.array.currency_symbol).toList()

        initUI()

        getMessage()

        initListeners()
        textWatchers()
    }

    private fun initUI() {

        includeBinding.dateTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate, "dd-MMMM-yyyy"
            )

        setUpCurrencySymbolList()
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddBorrowerPaymentFragmentArgs.fromBundle(it)
                }

                receivedBorrowerKey = args?.borrowerKeyMessage!!

                getBorrower()

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBorrower() {

        borrowerViewModel.getBorrowerByKey(receivedBorrowerKey).observe(viewLifecycleOwner, {

            if (it != null) {
                receivedBorrower = it
                Log.d(TAG, "getBorrower: received borrower : $receivedBorrower")

                includeBinding.borrowerNameTV.text = receivedBorrower!!.name
            }
        })
    }

    private fun setUpCurrencySymbolList() {

        includeBinding.moneySymbolSpinner.let { spinner ->

            spinner.adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                currencySymbols
            )

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                    spinner.setSelection(0)
                    selectedCurrencySymbol = currencySymbols[0]
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    spinner.setSelection(position)
                    selectedCurrencySymbol = currencySymbols[position]
                }
            }
        }

    }

    private fun initListeners() {

        includeBinding.selectDateBtn.setOnClickListener {

            Functions.showCalendarDialog(
                selectedDate,
                {
                    requireActivity().supportFragmentManager
                },
                { newDate ->

                    selectedDate = newDate
                    initUI()
                }
            )
        }
        includeBinding.calculateInterestBtn.setOnClickListener {

            //todo : handle this button
        }

        binding.addBorrowerPaymentToolBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        includeBinding.addInterestCB.setOnCheckedChangeListener(this)
        includeBinding.addSupprtingDocCB.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.addInterestCB.id -> showHideInterestCardView(isChecked)

            includeBinding.addSupprtingDocCB.id -> showHideAddSupportingDocCardView(isChecked)
        }

    }

    private fun textWatchers() {

        includeBinding.borrowerPaymentET.editText?.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.borrowerPaymentET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.borrowerPaymentET.error = null
            }
        }

        includeBinding.ratePercentET.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.ratePercentET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.ratePercentET.error = null
            }
        }

        includeBinding.fileNameET.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.fileNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.fileNameET.error = null
            }
        }
    }


    private fun showHideInterestCardView(isVisible: Boolean) {

        includeBinding.interestCV.isVisible = isVisible
    }

    private fun showHideAddSupportingDocCardView(isVisible: Boolean) {

        includeBinding.supportingDocCV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
