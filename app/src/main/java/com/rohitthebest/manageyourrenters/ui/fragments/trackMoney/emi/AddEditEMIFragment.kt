package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.AddEmiLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEmiBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.others.Constants.SUPPORTING_DOCUMENT_BOTTOM_SHEET_DISMISS_LISTENER_KEY
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AddEditEMIFragment"

@AndroidEntryPoint
class AddEditEMIFragment : Fragment(R.layout.fragment_add_emi), View.OnClickListener {

    private var _binding: FragmentAddEmiBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private lateinit var includeBinding: AddEmiLayoutBinding
    private lateinit var currencySymbolList: List<String>
    private var selectedCurrencySymbol = ""
    private var selectedEMIStartDate: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEmiBinding.bind(view)

        includeBinding = binding.includeLayout

        currencySymbolList = resources.getStringArray(R.array.currency_symbol).asList()

        selectedEMIStartDate = System.currentTimeMillis()
        includeBinding.emiStartDateTV.setDateInTextView(
            selectedEMIStartDate
        )

        initListeners()
        textWatcher()
        setUpCurrencySymbolSpinner()
        observeForSupportingDocumentBottomSheetDismissListener()
    }

    private fun observeForSupportingDocumentBottomSheetDismissListener() {

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(SUPPORTING_DOCUMENT_BOTTOM_SHEET_DISMISS_LISTENER_KEY)
            ?.observe(viewLifecycleOwner, {

                if (it) {

                    //todo : call back pressed
                    Log.d(TAG, "observeForSupportingDocumentBottomSheetDismissListener: $it")
                }
            })
    }

    private fun initListeners() {

        includeBinding.emiStartDateTV.setOnClickListener(this)
        includeBinding.emiStartDateCalendarBtn.setOnClickListener(this)

        binding.addEmiToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addEmiToolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

//            val action =
//                AddEditEMIFragmentDirections.actionAddEditEMIFragmentToAddSupportingDocumentBottomSheetFragment(
//                    "key", "tag"
//                )
//
//            findNavController().navigate(action)


//            if (isFormValid()) {
//
//                // todo : save to emi database
//            }
            true
        }
    }


    override fun onClick(v: View?) {

        if (v?.id == includeBinding.emiStartDateTV.id || v?.id == includeBinding.emiStartDateCalendarBtn.id) {

            Functions.showCalendarDialog(
                selectedEMIStartDate,
                {
                    requireActivity().supportFragmentManager
                },
                {
                    selectedEMIStartDate = it
                    includeBinding.emiStartDateTV.setDateInTextView(
                        selectedEMIStartDate
                    )
                }
            )
        }

    }

    private fun isFormValid(): Boolean {

        if (!includeBinding.emiNameET.editText?.isTextValid()!!) {

            includeBinding.emiNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.totalEmiMonthsET.isTextValid()) {

            includeBinding.emiNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.numberOfMonthsCompltedET.isTextValid()) {

            includeBinding.numberOfMonthsCompltedET.setText("0")
        }

        if (!isTotalMonthAndMonthCompletedETValid()) {

            includeBinding.totalEmiMonthsET.error =
                "It should be greater than or equal to the completed month."

            includeBinding.numberOfMonthsCompltedET.error =
                "It should be less than or equal to total months."

            return false
        }

        return true
    }


    private fun setUpCurrencySymbolSpinner() {

        includeBinding.moneySymbolSpinner.setCurrencySymbol(
            requireContext()
        ) { position ->

            selectedCurrencySymbol = currencySymbolList[position]
            calculateTotalEmiAmount()
        }
    }

    private fun textWatcher() {

        includeBinding.emiNameET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.emiNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.emiNameET.error = null
            }
        }

        includeBinding.totalEmiMonthsET.onTextChangedListener { s ->

            calculateTotalEmiAmount()

            if (s?.isEmpty()!!) {

                includeBinding.totalEmiMonthsET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.totalEmiMonthsET.error = null

                if (includeBinding.numberOfMonthsCompltedET.isTextValid()) {

                    // if completed month is greater than total month :
                    if (!isTotalMonthAndMonthCompletedETValid()) {

                        includeBinding.totalEmiMonthsET.error =
                            "It should be greater than or equal to the completed month."
                    } else {

                        includeBinding.totalEmiMonthsET.error = null
                    }
                }
            }
        }

        includeBinding.numberOfMonthsCompltedET.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.numberOfMonthsCompltedET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                if (includeBinding.totalEmiMonthsET.isTextValid()) {

                    if (!isTotalMonthAndMonthCompletedETValid()) {

                        includeBinding.numberOfMonthsCompltedET.error =
                            "It should be less than or equal to total months."
                    } else {

                        includeBinding.numberOfMonthsCompltedET.error = null
                    }
                }
            }
        }

        includeBinding.emiAmountPerMonthET.onTextChangedListener { s ->

            calculateTotalEmiAmount()

            if (s?.isEmpty()!!) {

                includeBinding.emiAmountPerMonthET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.emiAmountPerMonthET.error = null
            }
        }
    }

    var calculateTotalEMIJob: Job? = null

    @SuppressLint("SetTextI18n")
    private fun calculateTotalEmiAmount() {

        try {

            if (calculateTotalEMIJob != null && calculateTotalEMIJob?.isActive == true) {

                calculateTotalEMIJob?.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            calculateTotalEMIJob = lifecycleScope.launch {

                delay(200)

                val totalMonths =
                    if (includeBinding.totalEmiMonthsET.isTextValid()) {

                        includeBinding.totalEmiMonthsET.text.toString()
                            .toInt()
                    } else 0

                val amountPerMonth = if (includeBinding.emiAmountPerMonthET.isTextValid()) {

                    includeBinding.emiAmountPerMonthET.text.toString().toDouble()
                } else 0.0

                val totalEMIAmount = totalMonths * amountPerMonth

                includeBinding.totalEMIAmountTV.text = String.format(
                    "$totalMonths * $amountPerMonth = $selectedCurrencySymbol %.3f", totalEMIAmount
                )

            }
        }


    }

    private fun isTotalMonthAndMonthCompletedETValid(): Boolean {

        if (includeBinding.numberOfMonthsCompltedET.text.toString().toInt() >
            includeBinding.totalEmiMonthsET.text.toString().toInt()
        ) {

            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
