package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.Interest
import com.rohitthebest.manageyourrenters.data.InterestTimeSchedule
import com.rohitthebest.manageyourrenters.data.InterestType
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.AddBorrowerPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_TAG_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.others.Constants.IS_DOCUMENT_FOR_EDITING_KEY
import com.rohitthebest.manageyourrenters.ui.fragments.AddSupportingDocumentBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AddBorrowerPaymentFragm"

@AndroidEntryPoint
class AddBorrowerPaymentFragment : Fragment(R.layout.fragment_add_borrower_payment),
    CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener,
    View.OnClickListener, AddSupportingDocumentBottomSheetFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddBorrowerPaymentBinding? = null
    private val binding get() = _binding!!

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private var receivedBorrower: Borrower? = null
    private var receivedBorrowerKey: String = ""

    private lateinit var includeBinding: AddBorrowerPaymentLayoutBinding
    private lateinit var currencySymbols: List<String>
    private lateinit var interestTimeSchedules: List<String>
    private var selectedCurrencySymbol: String = ""
    private var selectedInterestTimeSchedule = InterestTimeSchedule.ANNUALLY

    private var selectedDate: Long = 0L
    private var interestType: InterestType = InterestType.SIMPLE_INTEREST

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBorrowerPaymentBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()

        //List of currency symbols of different places
        currencySymbols = resources.getStringArray(R.array.currency_symbol).toList()
        interestTimeSchedules = resources.getStringArray(R.array.interest_time_schedule).toList()

        initUI()

        getMessage()  // getting the message passed by thr BorrowerPaymentFragment

        initListeners()
        textWatchers()
        observeForSupportingDocumentBottomSheetDismissListener()
    }

    private fun observeForSupportingDocumentBottomSheetDismissListener() {

        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>(Constants.SUPPORTING_DOCUMENT_BOTTOM_SHEET_DISMISS_LISTENER_KEY)
            ?.observe(viewLifecycleOwner, {

                if (it) {

                    //todo : call back pressed
                    Log.d(TAG, "observeForSupportingDocumentBottomSheetDismissListener: $it")
                }
            })
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

    private fun initUI() {

        includeBinding.dateTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate, "dd-MMMM-yyyy"
            )

        setUpCurrencySymbolSpinner()
        setUpTimeScheduleSpinner()
    }

    private fun setUpTimeScheduleSpinner() {

        includeBinding.timeScheduleSpinner.apply {

            adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                interestTimeSchedules
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                    selectedInterestTimeSchedule = InterestTimeSchedule.ANNUALLY
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    setSelection(position)
                    selectedInterestTimeSchedule = when (position) {

                        0 -> InterestTimeSchedule.ANNUALLY

                        1 -> InterestTimeSchedule.MONTHLY

                        else -> InterestTimeSchedule.DAILY
                    }
                }
            }

        }
    }

    private fun setUpCurrencySymbolSpinner() {

        includeBinding.moneySymbolSpinner.setCurrencySymbol(
            requireContext()
        ) { position ->

            selectedCurrencySymbol = currencySymbols[position]
        }
    }

    private fun initListeners() {

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

                Log.d(TAG, "initListeners: Menu item clicked")
                if (isFormValid()) {

                    initBorrowerPayment()
                }
                true
            }
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
                //todo : handle this button
            }
        }
    }

    private fun isFormValid(): Boolean {

        if (!includeBinding.borrowerPaymentET.editText?.isTextValid()!!) {

            includeBinding.borrowerPaymentET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (includeBinding.borrowerPaymentET.editText?.text.toString().toDouble() <= 0) {

            includeBinding.borrowerPaymentET.error = "Please enter amount greater than 0."
            return false
        }

        if (includeBinding.addInterestCB.isChecked) {

            if (!includeBinding.ratePercentET.isTextValid()) {

                includeBinding.ratePercentET.requestFocus()
                includeBinding.ratePercentET.error = EDIT_TEXT_EMPTY_MESSAGE
                return false
            }
        }

        return includeBinding.borrowerPaymentET.editText?.isTextValid()!!
    }

    private fun initBorrowerPayment() {

        Log.d(TAG, "initBorrowerPayment: ")

        val borrowerPayment = BorrowerPayment()

        borrowerPayment.modified = System.currentTimeMillis()
        borrowerPayment.isSynced = false
        borrowerPayment.currencySymbol = selectedCurrencySymbol

        borrowerPayment.apply {

            created = selectedDate
            borrowerId = receivedBorrower?.borrowerId!!
            borrowerKey = receivedBorrowerKey
            amountTakenOnRent =
                includeBinding.borrowerPaymentET.editText?.text.toString().toDouble()
            dueLeftAmount = amountTakenOnRent
            isDueCleared = false

            isInterestAdded = includeBinding.addInterestCB.isChecked

            if (isInterestAdded) {

                interest = Interest(
                    interestType,
                    includeBinding.ratePercentET.text.toString().toDouble(),
                    selectedInterestTimeSchedule
                )
            }

            uid = getUid()!!

            key = generateKey("_${uid}")

            messageOrNote = includeBinding.addNoteET.text.toString()
        }

        showDialogForAskingIfTheUserNeedsToUploadSupportingDoc(borrowerPayment)
    }

    private fun showDialogForAskingIfTheUserNeedsToUploadSupportingDoc(borrowerPayment: BorrowerPayment) {

        // showing dialog if the user wants to upload any supporting document
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add supporting document")
            .setMessage("Do you want to add any supporting document?")
            .setPositiveButton("No") { dialog, _ ->

                // if user selects no, then simply insert the borrower payment to the cloud as well
                // as local database
                insertToDatabase(borrowerPayment)
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { dialog, _ ->

                // opening  bottomSheet for adding supporting document
                // and also sending this borrower payment instance and the collection name
                // as a bundle to the bottomSheet arguments
                // if the user adds a supporting document then insertion of borrower payment
                // to the database will be done there only

                if (isInternetAvailable(requireContext())) {

                    requireActivity().supportFragmentManager.let {

                        val bundle = Bundle()
                        bundle.putString(COLLECTION_TAG_KEY, getString(R.string.borrowerPayments))
                        bundle.putString(DOCUMENT_KEY, fromBorrowerPaymentToString(borrowerPayment))
                        bundle.putBoolean(IS_DOCUMENT_FOR_EDITING_KEY, false)

                        AddSupportingDocumentBottomSheetFragment.newInstance(
                            bundle
                        ).apply {
                            show(it, "AddSupportingDocTag")
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

        Log.d(TAG, "onBottomSheetDismissed: $isDocumentAdded")

        if (isDocumentAdded) {

            requireActivity().onBackPressed()
        }
    }


    private fun insertToDatabase(borrowerPayment: BorrowerPayment) {

        Log.d(TAG, "insertToDatabase: ")

        borrowerPayment.isSupportingDocAdded = false
        if (isInternetAvailable(requireContext())) {

            borrowerPayment.isSynced = true

            uploadDocumentToFireStore(
                requireContext(),
                getString(R.string.borrowerPayments),
                borrowerPayment.key
            )
        } else {

            borrowerPayment.isSynced = false
        }

        borrowerPaymentViewModel.insertBorrowerPayment(
            requireContext(), borrowerPayment
        )

        lifecycleScope.launch {

            delay(100)

            requireActivity().onBackPressed()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        hideKeyBoard(requireActivity())

        when (buttonView?.id) {

            includeBinding.addInterestCB.id -> showInterestCardView(isChecked)

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
