package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.AddEmiPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEmiPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.AddSupportingDocumentBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEmiPaymentFragment : Fragment(R.layout.fragment_add_emi_payment), View.OnClickListener,
    AddSupportingDocumentBottomSheetFragment.OnBottomSheetDismissListener {

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

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                EMIPaymentFragmentArgs.fromBundle(it)
            }

            receivedEMIKey = args?.emiKeyMessage!!

            getEMI()
        }
    }

    private fun getEMI() {

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner, { emi ->

            receivedEMI = emi

            binding.addEmiPaymentToolbar.title = "${receivedEMI.emiName} payment"
            getPreviousEMIPayment()
        })
    }


    private fun getPreviousEMIPayment() {

        emiPaymentViewModel.getAllEMIPaymentsByEMIKey(receivedEMIKey)
            .observe(viewLifecycleOwner) { emiPayments ->

                if (emiPayments.isNotEmpty()) {

                    // previous payment
                    previousEmiPayment =
                        emiPayments[0] // getting the first element as the list is in descending order
                }

                initUI()
            }
    }

    private fun initListeners() {

        binding.addEmiPaymentToolbar.setNavigationOnClickListener {

            hideKeyBoard(requireActivity())
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
                    updateSelectedDate()
                }
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initUI() {

        if (this::receivedEMI.isInitialized) {

            includeBinding.currentMonthStatusTV.text =
                "${receivedEMI.monthsCompleted} / ${receivedEMI.totalMonths}"
            includeBinding.emiPaymentFromMonthTV.text = "${receivedEMI.monthsCompleted + 1}"
            includeBinding.emiPaymentTillMonthET.setText("${receivedEMI.monthsCompleted + 1}")
            updateNumberOfMonthsSelectedTVAndAmountPaid()
            includeBinding.emiPaymentAmountPaidET.editText?.setText("${receivedEMI.amountPaidPerMonth}")

        } else {

            getPreviousEMIPayment()
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
            emiKey = receivedEMIKey
            fromMonth = includeBinding.emiPaymentFromMonthTV.text.toString().toInt()
            tillMonth = includeBinding.emiPaymentTillMonthET.text.toString().toInt()
            isSupportingDocumentAdded = false
            uid = receivedEMI.uid
            key = generateKey("_$uid")

        }

        showDialogForAskingIfTheUserNeedsToUploadSupportingDoc(emiPayment)
    }

    private fun showDialogForAskingIfTheUserNeedsToUploadSupportingDoc(emiPayment: EMIPayment) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add supporting document")
            .setMessage("Do you want to add any supporting document?")
            .setPositiveButton("No") { dialog, _ ->

                // if user selects no, then simply insert the emi to the cloud as well
                // as local database
                insertToDatabase(emiPayment)
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { dialog, _ ->

                // opening  bottomSheet for adding supporting document
                // and also sending this emiPayment instance and the collection name
                // as a bundle to the bottomSheet arguments
                // if the user adds a supporting document then insertion of emiPayment to the database will
                // be handled there only
                if (Functions.isInternetAvailable(requireContext())) {

                    requireActivity().supportFragmentManager.let { fragmentManager ->

                        val bundle = Bundle()
                        bundle.putString(
                            Constants.COLLECTION_TAG_KEY,
                            getString(R.string.emiPayments)
                        )
                        bundle.putString(Constants.DOCUMENT_KEY, fromEMIPaymentToString(emiPayment))
                        bundle.putBoolean(Constants.IS_DOCUMENT_FOR_EDITING_KEY, false)

                        AddSupportingDocumentBottomSheetFragment.newInstance(
                            bundle
                        ).apply {
                            show(fragmentManager, "AddSupportingDocTag")
                        }.setOnBottomSheetDismissListener(this)
                    }

                } else {

                    Functions.showToast(
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


    private fun insertToDatabase(emiPayment: EMIPayment) {

        emiPayment.isSynced = Functions.isInternetAvailable(requireContext())

        emiPayment.isSupportingDocumentAdded = false

        emiPaymentViewModel.insertEMIPayment(requireContext(), emiPayment)

        if (emiPayment.isSynced) {

            uploadDocumentToFireStore(
                requireContext(),
                fromEMIPaymentToString(emiPayment),
                getString(R.string.emiPayments),
                emiPayment.key
            )
        }

        requireActivity().onBackPressed()
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

        if (tillMonth > receivedEMI.totalMonths) {

            includeBinding.emiPaymentTillMonthET.error = "It cannot be greater than total months!!!"
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

    @SuppressLint("SetTextI18n")
    private fun updateNumberOfMonthsSelectedTVAndAmountPaid() {

        if (includeBinding.emiPaymentTillMonthET.isTextValid()) {

            val fromMonth = includeBinding.emiPaymentFromMonthTV.text.toString().toInt()
            val tillMonth = includeBinding.emiPaymentTillMonthET.text.toString().toInt()

            if (tillMonth >= fromMonth) {

                val numOfMonths = (tillMonth - fromMonth) + 1

                includeBinding.numberOfMonthsSelectedTV.text =
                    "Number of months selected : $numOfMonths"

                includeBinding.emiPaymentAmountPaidET
                    .editText?.setText((numOfMonths * receivedEMI.amountPaidPerMonth).toString())

            } else {

                includeBinding.emiPaymentAmountPaidET.editText?.setText("0")

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

                val totalEMIAmountLeft =
                    (receivedEMI.amountPaidPerMonth * receivedEMI.totalMonths) - receivedEMI.amountPaid

                val amount = includeBinding.emiPaymentAmountPaidET.editText?.text.toString().trim()
                    .toDouble()

                if (amount > totalEMIAmountLeft) {

                    includeBinding.emiPaymentAmountPaidET.error =
                        "Should be less than or equal to ${receivedEMI.currencySymbol} $totalEMIAmountLeft"
                } else {

                    includeBinding.emiPaymentAmountPaidET.error = null
                }
            }
        }

        includeBinding.emiPaymentTillMonthET.addTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.emiPaymentTillMonthET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.emiPaymentTillMonthET.error = null

                val tillMonth = includeBinding.emiPaymentTillMonthET.text.toString().toInt()
                if (tillMonth > receivedEMI.totalMonths) {

                    includeBinding.emiPaymentTillMonthET.error =
                        "It cannot be greater than total months!!!"
                } else {

                    updateNumberOfMonthsSelectedTVAndAmountPaid()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }

}
