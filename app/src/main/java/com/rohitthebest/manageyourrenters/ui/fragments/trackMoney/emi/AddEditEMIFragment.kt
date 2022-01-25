package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.AddEmiLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEmiBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.AddSupportingDocumentBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AddEditEMIFragment"

@AndroidEntryPoint
class AddEditEMIFragment : Fragment(R.layout.fragment_add_emi), View.OnClickListener,
    AddSupportingDocumentBottomSheetFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddEmiBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private lateinit var includeBinding: AddEmiLayoutBinding
    private lateinit var currencySymbolList: List<String>
    private var selectedCurrencySymbol = ""
    private var selectedEMIStartDate: Long = 0L

    //editing vars
    private var isMessageReceivedForEditing = false
    private var receivedEmiKey = ""
    private lateinit var receivedEmi: EMI

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

        lifecycleScope.launch {

            delay(150)
            getMessage()
        }

    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                AddEditEMIFragmentArgs.fromBundle(it)
            }

            receivedEmiKey = args?.emiKey!!
            isMessageReceivedForEditing = true
            getEmi()
        }
    }

    private fun getEmi() {

        emiViewModel.getEMIByKey(receivedEmiKey).observe(viewLifecycleOwner, {

            receivedEmi = it

            updateUI()
        })
    }

    private fun updateUI() {

        if (this::receivedEmi.isInitialized) {

            includeBinding.apply {

                emiNameET.editText?.setText(receivedEmi.emiName)
                emiStartDateTV.setDateInTextView(
                    receivedEmi.startDate
                )
                selectedEMIStartDate = receivedEmi.startDate

                selectedCurrencySymbol = receivedEmi.currencySymbol
                moneySymbolSpinner.setSelection(currencySymbolList.indexOf(receivedEmi.currencySymbol))

                totalEmiMonthsET.setText(receivedEmi.totalMonths.toString())
                numberOfMonthsCompltedET.setText(receivedEmi.monthsCompleted.toString())
                emiAmountPerMonthET.setText(receivedEmi.amountPaidPerMonth.toString())
                calculateTotalEmiAmount()
                amountPaidTillNowForEMITV.text = receivedEmi.amountPaid.toString()
            }
        }
    }

    private fun initListeners() {

        includeBinding.emiStartDateTV.setOnClickListener(this)
        includeBinding.emiStartDateCalendarBtn.setOnClickListener(this)

        binding.addEmiToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addEmiToolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (isFormValid()) {

                initEMI()
            }
            true
        }
    }

    private fun initEMI() {

        var emi = EMI()

        if (isMessageReceivedForEditing) {

            emi = receivedEmi
        }

        emi.modified = System.currentTimeMillis()
        emi.isSynced = false

        emi.apply {

            created =
                if (!isMessageReceivedForEditing) System.currentTimeMillis() else receivedEmi.created

            emiName = includeBinding.emiNameET.editText?.text.toString().trim()
            startDate = selectedEMIStartDate
            totalMonths = includeBinding.totalEmiMonthsET.text.toString().toInt()
            monthsCompleted = includeBinding.numberOfMonthsCompltedET.text.toString().toInt()
            amountPaidPerMonth = includeBinding.emiAmountPerMonthET.text.toString().toDouble()
            amountPaid = calculateAmountPaidTillNow()
            currencySymbol = selectedCurrencySymbol
            uid = getUid()!!

            key = if (!isMessageReceivedForEditing) generateKey("_${uid}") else receivedEmi.key
        }

        if (!isMessageReceivedForEditing) {

            showDialogForAskingIfTheUserNeedsToUploadSupportingDoc(emi)
        } else {

            insertToDatabase(emi)
        }
    }

    private fun showDialogForAskingIfTheUserNeedsToUploadSupportingDoc(emi: EMI) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add supporting document")
            .setMessage("Do you want to add any supporting document?")
            .setPositiveButton("No") { dialog, _ ->

                // if user selects no, then simply insert the emi to the cloud as well
                // as local database
                insertToDatabase(emi)
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { dialog, _ ->

                // opening  bottomSheet for adding supporting document
                // and also sending this emi instance and the collection name
                // as a bundle to the bottomSheet arguments
                // if the user adds a supporting document then insertion of emi to the database will
                // be done there only
                if (isInternetAvailable(requireContext())) {

                    requireActivity().supportFragmentManager.let { fragmentManager ->

                        val bundle = Bundle()
                        bundle.putString(Constants.COLLECTION_TAG_KEY, getString(R.string.emis))
                        bundle.putString(Constants.DOCUMENT_KEY, fromEMIToString(emi))
                        bundle.putBoolean(Constants.IS_DOCUMENT_FOR_EDITING_KEY, false)

                        AddSupportingDocumentBottomSheetFragment.newInstance(
                            bundle
                        ).apply {
                            show(fragmentManager, "AddSupportingDocTag")
                        }.setOnBottomSheetDismissListener(this)
                    }

                } else {

                    showToast(
                        requireContext(),
                        "Internet connection is needed for uploading supporting document.",
                        Toast.LENGTH_LONG
                    )
                }

                dialog.dismiss()
            }
            .create()
            .show()

    }

    override fun onBottomSheetDismissed(isDocumentAdded: Boolean) {

        if (isDocumentAdded) {

            requireActivity().onBackPressed()
        }
    }

    private fun insertToDatabase(emi: EMI) {

        Log.d(TAG, "insertToDatabase: ")

        emi.isSynced = isInternetAvailable(requireContext())

        if (!isMessageReceivedForEditing) {

            emi.isSupportingDocumentAdded = false

            emiViewModel.insertEMI(emi)
            Log.d(TAG, "insertToDatabase: EMI inserted")
        } else {

            emiViewModel.updateEMI(emi)
            requireContext().showToast(
                "EMI details updated"
            )
        }

        if (emi.isSynced) {

            uploadDocumentToFireStore(
                requireContext(),
                getString(R.string.emis),
                emi.key
            )
        }


        requireActivity().onBackPressed()
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

            if (!s?.trim()?.isEmpty()!!) {

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

        includeBinding.numberOfMonthsCompltedET.onTextChangedListener { s ->

            if (s?.isNotEmpty()!!) {

                calculateTotalEmiAmount()
            }
        }
    }

    private var calculateTotalEMIJob: Job? = null

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

                includeBinding.amountPaidTillNowForEMITV.text =
                    "$selectedCurrencySymbol ${calculateAmountPaidTillNow()}"

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateAmountPaidTillNow(): Double {

        var amountPaidTillNow = 0.0

        if (includeBinding.numberOfMonthsCompltedET.isTextValid()
            && includeBinding.totalEmiMonthsET.isTextValid()
            && includeBinding.emiAmountPerMonthET.isTextValid()
        ) {

            amountPaidTillNow = includeBinding.numberOfMonthsCompltedET.text.toString()
                .toInt() * includeBinding.emiAmountPerMonthET.text.toString()
                .toDouble()
        }

        return amountPaidTillNow
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
