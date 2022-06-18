package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.AddBorrowerPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.calculateNumberOfDays
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddBorrowerPaymentFragment"

@AndroidEntryPoint
class AddBorrowerPaymentFragment : Fragment(R.layout.fragment_add_borrower_payment),
    CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener,
    View.OnClickListener, SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddBorrowerPaymentBinding? = null
    private val binding get() = _binding!!

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private var receivedBorrower: Borrower? = null
    private var receivedBorrowerKey: String = ""
    private var receivedBorrowerPayment: BorrowerPayment? = null

    private var isMessageReceivedForEditing = false

    private lateinit var includeBinding: AddBorrowerPaymentLayoutBinding
    private lateinit var currencySymbols: List<String>
    private lateinit var interestTimeSchedules: List<String>
    private var selectedCurrencySymbol: String = ""
    private var selectedInterestTimeSchedule = InterestTimeSchedule.ANNUALLY

    private var selectedDate: Long = 0L
    private var interestType: InterestType = InterestType.SIMPLE_INTEREST

    private lateinit var supportingDocmtHelperModel: SupportingDocumentHelperModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBorrowerPaymentBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()

        //List of currency symbols of different places
        currencySymbols = resources.getStringArray(R.array.currency_symbol).toList()
        interestTimeSchedules = resources.getStringArray(R.array.interest_time_schedule).toList()

        supportingDocmtHelperModel = SupportingDocumentHelperModel()

        initUI()

        getMessage()  // getting the message passed by thr BorrowerPaymentFragment

        initListeners()
        textWatchers()
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddBorrowerPaymentFragmentArgs.fromBundle(it)
                }

                receivedBorrowerKey = args?.borrowerKeyMessage!!
                getBorrower()

                val borrowerPaymentKey = args.borrowerPaymentKey
                if (borrowerPaymentKey.isValid()) {

                    isMessageReceivedForEditing = true
                    getBorrowerPayment(borrowerPaymentKey!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBorrowerPayment(borrowerPaymentKey: String) {

        borrowerPaymentViewModel.getBorrowerPaymentByKey(borrowerPaymentKey)
            .observe(viewLifecycleOwner) { payment ->

                receivedBorrowerPayment = payment

                updateUI()
            }
    }

    private fun updateUI() {

        binding.addBorrowerPaymentToolBar.title = getString(R.string.edit_payment)

        if (receivedBorrowerPayment != null) {

            includeBinding.moneySymbolSpinner.setSelection(
                currencySymbols.indexOf(
                    receivedBorrowerPayment!!.currencySymbol
                )
            )
            selectedDate = receivedBorrowerPayment!!.created
            includeBinding.dateTV.setDateInTextView(selectedDate)
            includeBinding.borrowerAmountET.editText?.setText(receivedBorrowerPayment!!.amountTakenOnRent.toString())

            // interest
            if (receivedBorrowerPayment!!.isInterestAdded) {

                includeBinding.addInterestCB.isChecked = true
                onCheckedChanged(includeBinding.addInterestCB, true)

                includeBinding.timeScheduleSpinner.setSelection(
                    when (receivedBorrowerPayment!!.interest?.timeSchedule) {

                        InterestTimeSchedule.ANNUALLY -> 0
                        InterestTimeSchedule.MONTHLY -> 1
                        InterestTimeSchedule.DAILY -> 2
                        else -> 0
                    }
                )
                if (receivedBorrowerPayment!!.interest?.type == InterestType.SIMPLE_INTEREST) {

                    includeBinding.interestTypeRG.check(includeBinding.simpleIntRB.id)
                } else {

                    includeBinding.interestTypeRG.check(includeBinding.compundIntRB.id)
                }

                includeBinding.ratePercentET.setText(receivedBorrowerPayment!!.interest?.ratePercent.toString())
            }
            includeBinding.addNoteET.setText(receivedBorrowerPayment!!.messageOrNote)

            includeBinding.addSupportingDocCB.hide()
            includeBinding.viewEditSupportingDoc.hide()
        }

    }

    private fun getBorrower() {

        borrowerViewModel.getBorrowerByKey(receivedBorrowerKey).observe(viewLifecycleOwner) {

            if (it != null) {
                receivedBorrower = it
                Log.d(TAG, "getBorrower: received borrower : $receivedBorrower")

                includeBinding.borrowerNameTV.text = receivedBorrower!!.name
            }
        }
    }

    private fun initUI() {

        includeBinding.dateTV.setDateInTextView(selectedDate)

        setUpCurrencySymbolSpinner()
        setUpTimeScheduleSpinner()
    }

    private fun setUpTimeScheduleSpinner() {

        includeBinding.timeScheduleSpinner.setListToSpinner(
            requireContext(),
            interestTimeSchedules,
            { position ->
                selectedInterestTimeSchedule = when (position) {

                    0 -> InterestTimeSchedule.ANNUALLY

                    1 -> InterestTimeSchedule.MONTHLY

                    else -> InterestTimeSchedule.DAILY
                }
            }, {}
        )
    }

    private fun setUpCurrencySymbolSpinner() {

        includeBinding.moneySymbolSpinner.setListToSpinner(
            requireContext(), currencySymbols,
            { position -> selectedCurrencySymbol = currencySymbols[position] }, {}
        )
    }

    private fun initListeners() {

        includeBinding.addSupportingDocCB.isChecked = false

        includeBinding.selectDateBtn.setOnClickListener(this)
        includeBinding.dateTV.setOnClickListener(this)
        includeBinding.calculateInterestBtn.setOnClickListener(this)

        binding.addBorrowerPaymentToolBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        includeBinding.addInterestCB.setOnCheckedChangeListener(this)
        includeBinding.interestTypeRG.setOnCheckedChangeListener(this)

        binding.addBorrowerPaymentToolBar.menu.findItem(R.id.menu_save_btn)
            .setOnMenuItemClickListener {
                if (isFormValid()) {

                    initBorrowerPayment()
                }
                true
            }

        includeBinding.addSupportingDocCB.setOnCheckedChangeListener(this)
        includeBinding.viewEditSupportingDoc.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        if (v?.id == includeBinding.dateTV.id || v?.id == includeBinding.selectDateBtn.id) {

            showCalendarDialog(
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

        when (v?.id) {

            includeBinding.calculateInterestBtn.id -> {

                if (isFormValid()) {

                    handleCalculateInterestBtn()
                }
            }

            includeBinding.viewEditSupportingDoc.id -> {

                showSupportDocumentBottomSheetDialog()
            }
        }
    }

    private fun handleCalculateInterestBtn() {

        val interestCalculatorFields = InterestCalculatorFields(
            selectedDate,
            includeBinding.borrowerAmountET.editText?.text.toString().toDouble(),
            Interest(
                if (includeBinding.interestTypeRG.checkedRadioButtonId == includeBinding.simpleIntRB.id) {

                    InterestType.SIMPLE_INTEREST
                } else {
                    InterestType.COMPOUND_INTEREST
                },
                includeBinding.ratePercentET.text.toString().toDouble(),
                selectedInterestTimeSchedule
            ),
            calculateNumberOfDays(selectedDate, System.currentTimeMillis())
        )

        val action =
            AddBorrowerPaymentFragmentDirections.actionAddBorrowerPaymentFragmentToCalculateInterestBottomSheetFragment(
                interestCalculatorFields.convertToJsonString()
            )

        findNavController().navigate(action)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        hideKeyBoard(requireActivity())

        when (buttonView?.id) {

            includeBinding.addInterestCB.id -> showInterestCardView(isChecked)

            includeBinding.addSupportingDocCB.id -> {

                if (isChecked) {

                    includeBinding.viewEditSupportingDoc.show()
                    supportingDocmtHelperModel = SupportingDocumentHelperModel()
                    supportingDocmtHelperModel.modelName = getString(R.string.borrowerPayments)
                    showSupportDocumentBottomSheetDialog()
                } else {

                    includeBinding.viewEditSupportingDoc.hide()
                }
            }
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        hideKeyBoard(requireActivity())

        when (checkedId) {

            includeBinding.simpleIntRB.id -> {

                interestType = InterestType.SIMPLE_INTEREST
            }

            includeBinding.compundIntRB.id -> {
                interestType = InterestType.COMPOUND_INTEREST
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


    private fun isFormValid(): Boolean {

        if (!includeBinding.borrowerAmountET.editText?.isTextValid()!!) {

            includeBinding.borrowerAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (includeBinding.borrowerAmountET.editText?.text.toString().toDouble() <= 0) {

            includeBinding.borrowerAmountET.error =
                getString(R.string.please_enter_amount_grater_than_0)
            return false
        }

        if (includeBinding.addInterestCB.isChecked) {

            if (!includeBinding.ratePercentET.isTextValid()) {

                includeBinding.ratePercentET.requestFocus()
                includeBinding.ratePercentET.error = EDIT_TEXT_EMPTY_MESSAGE
                return false
            }
        }

        return includeBinding.borrowerAmountET.editText?.isTextValid()!!
    }

    private fun initBorrowerPayment() {

        Log.d(TAG, "initBorrowerPayment: ")

        var borrowerPayment = BorrowerPayment()

        if (isMessageReceivedForEditing) {

            borrowerPayment = receivedBorrowerPayment!!.copy()
        }

        Log.d(TAG, "initBorrowerPayment: $borrowerPayment")

        borrowerPayment.modified = System.currentTimeMillis()
        borrowerPayment.isSynced = false

        borrowerPayment.apply {
            created = selectedDate

            currencySymbol = selectedCurrencySymbol
            amountTakenOnRent =
                includeBinding.borrowerAmountET.editText?.text.toString().toDouble()

            isInterestAdded = includeBinding.addInterestCB.isChecked
            dueLeftAmount = amountTakenOnRent

            interest = if (isInterestAdded) {

                Interest(
                    interestType,
                    includeBinding.ratePercentET.text.toString().toDouble(),
                    selectedInterestTimeSchedule
                )
            } else {
                null
            }
            if (!isMessageReceivedForEditing) {

                borrowerId = receivedBorrower?.borrowerId!!
                borrowerKey = receivedBorrowerKey
                uid = getUid()!!
                key = generateKey("_${uid}")
                isDueCleared = false

                if (includeBinding.addSupportingDocCB.isChecked) {

                    isSupportingDocAdded = true

                    if (supportingDocmtHelperModel.documentType == DocumentType.URL) {

                        supportingDocument = SupportingDocument(
                            supportingDocmtHelperModel.documentName,
                            supportingDocmtHelperModel.documentUrl,
                            supportingDocmtHelperModel.documentType
                        )
                    }
                }
            }

            messageOrNote = includeBinding.addNoteET.text.toString()
        }

        if (!isMessageReceivedForEditing
            && borrowerPayment.isSupportingDocAdded
            && supportingDocmtHelperModel.documentType != DocumentType.URL
        ) {

            // if the document type is not URL, then we need internet connection to upload the uri
            if (!isInternetAvailable(requireContext())) {
                showToast(
                    requireContext(),
                    getString(R.string.internet_required_message_for_uploading_doc),
                    Toast.LENGTH_LONG
                )
                return
            }
        }

        insertToDatabase(borrowerPayment)
    }

    private fun insertToDatabase(borrowerPayment: BorrowerPayment) {

        if (!isMessageReceivedForEditing) {

            // insert
            if (borrowerPayment.isSupportingDocAdded && supportingDocmtHelperModel.documentType != DocumentType.URL
            ) {
                supportingDocmtHelperModel.modelName = getString(R.string.borrowerPayments)
                borrowerPaymentViewModel.insertBorrowerPayment(
                    borrowerPayment,
                    supportingDocmtHelperModel
                )
            } else {
                borrowerPaymentViewModel.insertBorrowerPayment(borrowerPayment, null)
            }

            showToast(requireContext(), getString(R.string.payment_added))

        } else {

            // update
            borrowerPaymentViewModel.updateBorrowerPayment(
                receivedBorrowerPayment!!,
                borrowerPayment
            )
            showToast(requireContext(), getString(R.string.payment_updated))
        }

        requireActivity().onBackPressed()
    }

    private fun textWatchers() {

        includeBinding.borrowerAmountET.editText?.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.borrowerAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.borrowerAmountET.error = null
            }
        }

        includeBinding.ratePercentET.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.ratePercentET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.ratePercentET.error = null
            }
        }
    }

    private fun showInterestCardView(isVisible: Boolean) {

        includeBinding.interestCV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }

}
