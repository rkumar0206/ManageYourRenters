package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.InterestTimeSchedule
import com.rohitthebest.manageyourrenters.data.InterestType
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.AddBorrowerPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.checkIfPermissionsGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddBorrowerPaymentFragm"

@AndroidEntryPoint
class AddBorrowerPaymentFragment : Fragment(R.layout.fragment_add_borrower_payment),
    CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener,
    View.OnClickListener {

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
    private var docType = "pdf"
    private var interestType: InterestType = InterestType.SIMPLE_INTEREST

    private var pdfUri: Uri? = null
    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBorrowerPaymentBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()

        //List of currency symbols of different places
        currencySymbols = resources.getStringArray(R.array.currency_symbol).toList()
        interestTimeSchedules = resources.getStringArray(R.array.interest_time_schedule).toList()

        initUI()

        getMessage()

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

        includeBinding.moneySymbolSpinner.apply {

            adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                currencySymbols
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {

                    setSelection(0)
                    selectedCurrencySymbol = currencySymbols[0]
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    setSelection(position)
                    selectedCurrencySymbol = currencySymbols[position]
                }
            }
        }

    }

    private fun initListeners() {

        includeBinding.selectDateBtn.setOnClickListener(this)
        includeBinding.calculateInterestBtn.setOnClickListener(this)
        includeBinding.addFileMCV.setOnClickListener(this)
        includeBinding.removeFileBtn.setOnClickListener(this)

        binding.addBorrowerPaymentToolBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        includeBinding.addInterestCB.setOnCheckedChangeListener(this)
        includeBinding.addSupprtingDocCB.setOnCheckedChangeListener(this)
        includeBinding.docTypeRG.setOnCheckedChangeListener(this)
        includeBinding.interestTypeRG.setOnCheckedChangeListener(this)

    }


    override fun onClick(v: View?) {

        when (v?.id) {

            includeBinding.selectDateBtn.id -> {

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

            includeBinding.calculateInterestBtn.id -> {
                //todo : handle this button
            }

            includeBinding.addFileMCV.id -> {

                if (requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    if (docType == getString(R.string.pdf)) {

                        chooseDocumentLauncher.launch("application/pdf")
                    } else {

                        chooseDocumentLauncher.launch("image/*")
                    }

                } else {

                    requestPermission()
                }
            }

            includeBinding.removeFileBtn.id -> {

                if (docType == getString(R.string.pdf)) {

                    pdfUri = null
                } else {

                    imageUri = null
                }

                includeBinding.fileNameET.setText("")

                showRemoveFileBtn(false)
                showFileNameEditText(false)
                showAddFileBtn(true)
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.addInterestCB.id -> showInterestCardView(isChecked)

            includeBinding.addSupprtingDocCB.id -> showAddSupportingDocCardView(isChecked)
        }

    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        when (checkedId) {

            includeBinding.pdfRB.id -> {

                docType = getString(R.string.pdf)
                includeBinding.fileNameET.hint = "Enter file name"

                if (pdfUri != null) {

                    showFileNameEditText(true)
                    showRemoveFileBtn(true)
                    showAddFileBtn(false)
                    includeBinding.fileNameET.setText(pdfUri?.getFileName(requireActivity().contentResolver))

                } else {

                    showFileNameEditText(false)
                    showRemoveFileBtn(false)
                    showAddFileBtn(true)
                }

            }

            includeBinding.imageRB.id -> {

                includeBinding.fileNameET.hint = "Enter image name"
                docType = getString(R.string.image)

                if (imageUri != null) {

                    showFileNameEditText(true)
                    showRemoveFileBtn(true)
                    showAddFileBtn(false)
                    includeBinding.fileNameET.setText(imageUri?.getFileName(requireActivity().contentResolver))

                } else {

                    showFileNameEditText(false)
                    showRemoveFileBtn(false)
                    showAddFileBtn(true)
                }
            }

            includeBinding.urlRB.id -> {

                includeBinding.fileNameET.hint = "Enter url here"
                docType = getString(R.string.url)

                includeBinding.fileNameET.setText("")

                showFileNameEditText(true)
                showAddFileBtn(false)
                showRemoveFileBtn(false)
            }

            includeBinding.simpleIntRB.id -> {

                interestType = InterestType.SIMPLE_INTEREST
            }

            includeBinding.compundIntRB.id -> {
                interestType = InterestType.COMPOUND_INTEREST
            }
        }
    }

    // adding conditions for requesting permission
    private fun requestPermission() {

        when {

            //check if permission already granted
            requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE) -> {

                //permission is granted
            }

            // if the app deems that they should show the request permission rationale
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

                binding.root.showSnackbarWithActionAndDismissListener(
                    "Permission is required for selecting image from your storage.",
                    "Ok",
                    {
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    },
                    {
                        //null
                    }
                )
            }

            // request for permission
            else -> {

                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    //[START OF LAUNCHERS]
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            Log.i(TAG, "Permission granted: ")
        } else {
            Log.i(TAG, "Permission denied: ")
        }
    }

    private val chooseDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->

        uri?.let {

            if (docType == getString(R.string.pdf)) {

                //pdf uri
                pdfUri = uri
            } else {

                // image uri
                imageUri = uri
            }

            val fileName = it.getFileName(requireActivity().contentResolver)

            if (fileName.isValid()) {

                includeBinding.fileNameET.setText(fileName)
            }

            showAddFileBtn(false)
            showFileNameEditText(true)
            showRemoveFileBtn(true)
        }
    }
    //[END OF LAUNCHERS]

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

    private fun showInterestCardView(isVisible: Boolean) {

        includeBinding.interestCV.isVisible = isVisible
    }

    private fun showAddSupportingDocCardView(isVisible: Boolean) {

        includeBinding.supportingDocCV.isVisible = isVisible
    }

    private fun showAddFileBtn(isVisible: Boolean) {

        includeBinding.addFileMCV.isVisible = isVisible
    }

    private fun showFileNameEditText(isVisible: Boolean) {

        includeBinding.fileNameET.isVisible = isVisible
    }

    private fun showRemoveFileBtn(isVisible: Boolean) {

        includeBinding.removeFileBtn.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
