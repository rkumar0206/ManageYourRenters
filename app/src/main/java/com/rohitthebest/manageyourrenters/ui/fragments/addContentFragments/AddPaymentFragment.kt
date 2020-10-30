package com.rohitthebest.manageyourrenters.ui.fragments.addContentFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.AddPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPaymentBinding
import com.rohitthebest.manageyourrenters.ui.fragments.PaymentFragmentArgs
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPaymentFragment : Fragment(), View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddPaymentLayoutBinding

    private var receivedRenter: Renter? = null
    private var currentTimestamp = 0L

    private var monthList: List<String>? = null
    private var currencyList: List<String>? = null
    private var billMonth: String? = null
    private var currencySymbol: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeBinding = binding.include

        monthList = ArrayList()          //List of months
        currencyList = ArrayList()       //List of currency Symbols

        //List of months
        monthList = resources.getStringArray(R.array.months).toList()

        //List of currency symbols of different places
        currencyList = resources.getStringArray(R.array.currency_symbol).toList()

        setUpSpinnerMonth() //Setting the from(month) and till(month) spinners
        setUpCurrencySymbolList()

        getMessage()
        initListeners()
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                receivedRenter = ConversionWithGson.convertJSONtoRenter(args?.renterInfoMessage)
                initialChanges()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun initialChanges() {

        receivedRenter?.let {

            includeBinding.renterNameTV.text = it.name

            currentTimestamp = System.currentTimeMillis()
            includeBinding.dateTV.text = "Date : ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    currentTimestamp
                )
            }"

            includeBinding.timeTV.text = "Time : ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    currentTimestamp,
                    "hh:mm:ss"
                )
            }"
        }
    }

    private fun setUpSpinnerMonth() {

        includeBinding.monthSelectSpinner.let { spinner ->

            if (monthList != null) {
                spinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    monthList!!
                )

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                        spinner.setSelection(0)
                        billMonth = monthList!![0]
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        spinner.setSelection(position)
                        billMonth = monthList!![position]
                    }
                }
            }
        }

    }

    private fun setUpCurrencySymbolList() {

        includeBinding.moneySymbolSpinner.let { spinner ->

            if (currencyList != null) {
                spinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    currencyList!!
                )

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                        spinner.setSelection(0)
                        currencySymbol = currencyList!![0]
                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {

                        spinner.setSelection(position)
                        currencySymbol = currencyList!![position]
                        //showToast(requireContext(), "${currencyList!![position]} is selected..")
                    }
                }
            }
        }

    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        includeBinding.seeTotalBtn.setOnClickListener(this)

        includeBinding.periodTypeRG.setOnCheckedChangeListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {


            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }

        }

    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        if (checkedId == includeBinding.byMonthRB.id) {

            showByMonthAndHideByDateView()
        } else {

            hideByMonthAndShowByDateView()
        }

    }

    private fun showByMonthAndHideByDateView() {

        try {
            includeBinding.byDateCL.hide()
            includeBinding.monthSelectSpinner.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideByMonthAndShowByDateView() {

        try {
            includeBinding.byDateCL.show()
            includeBinding.monthSelectSpinner.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}