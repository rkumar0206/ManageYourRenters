package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.AddEmiPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEmiPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.setDateInTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEmiPaymentFragment : Fragment(R.layout.fragment_add_emi_payment), View.OnClickListener {

    private var _binding: FragmentAddEmiPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddEmiPaymentLayoutBinding

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()

    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI

    private var previousEmiPayment: EMIPayment? = null

    private var selectedDate = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEmiPaymentBinding.bind(view)

        includeBinding = binding.include
        getMessage()

        selectedDate = System.currentTimeMillis()
        updateSelectedDate()
        textWatcher()

        initListeners()
    }

    private fun initListeners() {

        binding.addEmiPaymentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addEmiPaymentToolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (isFormValid()) {

                initEMIPayment()
            }

            true
        }

        includeBinding.emiPaymentPaidOnTV.setOnClickListener(this)
        includeBinding.emiPaymentPaidOnIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        if (v?.id == includeBinding.emiPaymentPaidOnTV.id || v?.id == includeBinding.emiPaymentPaidOnIB.id) {

            showCalendarDialog(
                selectedDate,
                {
                    requireActivity().supportFragmentManager
                },
                { date ->

                    selectedDate = date
                }
            )
        }
    }

    private fun initEMIPayment() {

        val emiPayment = EMIPayment()

        emiPayment.modified = System.currentTimeMillis()
        emiPayment.isSynced = false

        emiPayment.apply {

            created = selectedDate
            emiKey = receivedEMIKey
            amountPaid = includeBinding.emiPaymentAmountPaidET.editText?.text.toString().toDouble()

            //todo
        }

    }

    private fun isFormValid(): Boolean {

        if (!includeBinding.emiPaymentTillMonthET.isTextValid()) {

            includeBinding.emiPaymentTillMonthET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }
        val fromMonth = includeBinding.emiPaymentFromMonthTV.text.toString().toInt()
        val tillMonth = includeBinding.emiPaymentTillMonthET.text.toString().toInt()

        if (tillMonth < fromMonth) {

            includeBinding.emiPaymentTillMonthET.requestFocus()
            includeBinding.emiPaymentTillMonthET.error =
                "It can't be less than the starting month!!!"
            return false
        }

        if (!includeBinding.emiPaymentAmountPaidET.editText?.isTextValid()!!) {

            includeBinding.emiPaymentAmountPaidET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return includeBinding.emiPaymentTillMonthET.error == null &&
                includeBinding.emiPaymentAmountPaidET.error == null
    }

    private fun updateSelectedDate() {

        includeBinding.emiPaymentPaidOnTV.setDateInTextView(selectedDate)
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                EMIPaymentFragmentArgs.fromBundle(it)
            }

            receivedEMIKey = args?.emiKeyMessage!!

            getEMI()
            getPreviousEMIPayment()
        }
    }

    private fun getEMI() {

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner, { emi ->

            receivedEMI = emi

            binding.addEmiPaymentToolbar.title = "${receivedEMI.emiName} payment"
        })
    }


    private fun getPreviousEMIPayment() {

        emiPaymentViewModel.getAllEMIPaymentsByEMIKey(receivedEMIKey)
            .observe(viewLifecycleOwner) { emiPayments ->

                if (emiPayments.isNotEmpty()) {

                    previousEmiPayment =
                        emiPayments[0] // getting the first element as the list is in descending order
                }

                initUI()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {

        includeBinding.currentMonthStatusTV.text =
            "${receivedEMI.monthsCompleted} / ${receivedEMI.totalMonths}"
        includeBinding.emiPaymentFromMonthTV.text = "${receivedEMI.monthsCompleted + 1}"
        includeBinding.emiPaymentTillMonthET.setText("${receivedEMI.monthsCompleted + 1}")
        updateNumberOfMonthsSelectedTVAndAmountPaid()
        includeBinding.emiPaymentAmountPaidET.editText?.setText("${receivedEMI.amountPaidPerMonth}")

    }

    @SuppressLint("SetTextI18n")
    private fun updateNumberOfMonthsSelectedTVAndAmountPaid() {

        if (includeBinding.emiPaymentTillMonthET.isTextValid()) {

            val fromMonth = includeBinding.emiPaymentFromMonthTV.text.toString().toInt()
            val tillMonth = includeBinding.emiPaymentTillMonthET.text.toString().toInt()

            if (tillMonth >= fromMonth) {

                val numOfMonths = (tillMonth - fromMonth) + 1
                includeBinding.numberOfMonthsSelectedTV.text =
                    "Number of months selected : $numOfMonths"
            } else {

                includeBinding.emiPaymentTillMonthET.error =
                    "It can't be less than the starting month!!!"
            }
        }
    }

    private fun textWatcher() {

        includeBinding.emiPaymentAmountPaidET.editText?.addTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.emiPaymentAmountPaidET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.emiPaymentAmountPaidET.error = null
            }
        }

        includeBinding.emiPaymentTillMonthET.addTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.emiPaymentAmountPaidET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.emiPaymentAmountPaidET.error = null
                updateNumberOfMonthsSelectedTVAndAmountPaid()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
