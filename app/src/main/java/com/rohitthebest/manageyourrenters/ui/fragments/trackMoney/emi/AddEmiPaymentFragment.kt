package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.AddEmiPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEmiPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddEmiPaymentFragment"

@AndroidEntryPoint
class AddEmiPaymentFragment : Fragment(R.layout.fragment_add_emi_payment), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddEmiPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddEmiPaymentLayoutBinding

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()

    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI

    private var previousEmiPayment: EMIPayment? = null

    private var selectedDate = 0L
    private lateinit var supportingDocmtHelperModel: SupportingDocumentHelperModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEmiPaymentBinding.bind(view)

        includeBinding = binding.include
        getMessage()
        supportingDocmtHelperModel = SupportingDocumentHelperModel()

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

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner) { emi ->

            receivedEMI = emi

            binding.addEmiPaymentToolbar.title = "${receivedEMI.emiName} payment"
            getPreviousEMIPayment()
        }
    }


    private fun getPreviousEMIPayment() {

        emiPaymentViewModel.getLastEMIPaymentOfEMIbyEMIKey(receivedEMIKey)
            .observe(viewLifecycleOwner) { emiPayment ->

                if (emiPayment != null) {

                    // previous payment
                    previousEmiPayment = emiPayment
                }

                initUI()
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


    private fun initListeners() {

        includeBinding.addSupportingDocCB.isChecked = false

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
        includeBinding.addSupportingDocCB.setOnCheckedChangeListener(this)
        includeBinding.viewEditSupportingDoc.setOnClickListener(this)
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

        when (v?.id) {

            includeBinding.viewEditSupportingDoc.id -> {
                showSupportDocumentBottomSheetDialog()
            }
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        hideKeyBoard(requireActivity())

        when (buttonView?.id) {

            includeBinding.addSupportingDocCB.id -> {

                if (isChecked) {

                    includeBinding.viewEditSupportingDoc.show()
                    supportingDocmtHelperModel = SupportingDocumentHelperModel()
                    supportingDocmtHelperModel.modelName = getString(R.string.emiPayments)
                    showSupportDocumentBottomSheetDialog()
                } else {

                    includeBinding.viewEditSupportingDoc.hide()
                }
            }
        }
    }

    private fun showSupportDocumentBottomSheetDialog() {

        val bundle = Bundle()
        bundle.putString(
            Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY,
            supportingDocmtHelperModel.convertToJsonString()
        )

        requireActivity().supportFragmentManager.let {

            SupportingDocumentDialogFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onBottomSheetDismissed(
        isDocumentAdded: Boolean,
        supportingDocumentHelperModel: SupportingDocumentHelperModel
    ) {

        if (!isDocumentAdded) {
            includeBinding.addSupportingDocCB.isChecked = false
        } else {

            supportingDocmtHelperModel = supportingDocumentHelperModel
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
            isSupportingDocAdded = false
            uid = receivedEMI.uid
            key = generateKey("_$uid")
            message = if (!includeBinding.addNoteET.isTextValid()) {
                ""
            } else {

                includeBinding.addNoteET.text.toString().trim()
            }

            isSupportingDocAdded = includeBinding.addSupportingDocCB.isChecked

            if (includeBinding.addSupportingDocCB.isChecked
                && supportingDocmtHelperModel.documentType == DocumentType.URL
            ) {
                SupportingDocument(
                    supportingDocmtHelperModel.documentName,
                    supportingDocmtHelperModel.documentUrl,
                    supportingDocmtHelperModel.documentType
                )
            }
        }

        if (emiPayment.isSupportingDocAdded
            && supportingDocmtHelperModel.documentType != DocumentType.URL
        ) {

            // if the document type is not URL, then we need internet connection to upload the uri
            if (!Functions.isInternetAvailable(requireContext())) {
                Functions.showToast(
                    requireContext(),
                    getString(R.string.internet_required_message_for_uploading_doc),
                    Toast.LENGTH_LONG
                )
                return
            }

            emiPaymentViewModel.insertEMIPayment(
                emiPayment,
                supportingDocmtHelperModel
            )
        } else {

            emiPaymentViewModel.insertEMIPayment(emiPayment, null)
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
