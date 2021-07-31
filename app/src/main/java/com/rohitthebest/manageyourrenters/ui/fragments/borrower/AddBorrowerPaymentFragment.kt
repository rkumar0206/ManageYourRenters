package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.AddBorrowerPaymentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.checkIfPermissionsGranted
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
    private var docType: DocumentType = DocumentType.PDF
    private var interestType: InterestType = InterestType.SIMPLE_INTEREST

    private var pdfUri: Uri? = null
    private var imageUri: Uri? = null
    private var fileSize = 0L

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
        includeBinding.addFileMCV.setOnClickListener(this)
        includeBinding.removeFileBtn.setOnClickListener(this)

        binding.addBorrowerPaymentToolBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        includeBinding.addInterestCB.setOnCheckedChangeListener(this)
        includeBinding.addSupprtingDocCB.setOnCheckedChangeListener(this)
        includeBinding.docTypeRG.setOnCheckedChangeListener(this)
        includeBinding.interestTypeRG.setOnCheckedChangeListener(this)

        binding.addBorrowerPaymentToolBar.menu.findItem(R.id.menu_save_btn)
            .setOnMenuItemClickListener {

                Log.d(TAG, "initListeners: Menu item clicked")
                checkFormAndInitDocument()
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

            includeBinding.addFileMCV.id -> {

                if (requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    if (docType == DocumentType.PDF) {

                        chooseDocumentLauncher.launch("application/pdf")
                    } else {

                        chooseDocumentLauncher.launch("image/*")
                    }

                } else {

                    requestPermission()
                }
            }

            includeBinding.removeFileBtn.id -> {

                if (docType == DocumentType.PDF) {

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

    private fun checkFormAndInitDocument() {

        Log.d(TAG, "checkFormAndInitDatabase: ")

        // checking if the form is valid and saving to database
        if (isFormValid()) {

            Log.d(TAG, "checkFormAndInitDatabase: isFormValid")

            // checking if the addSupportDocument enabled and the doc type is pdf or image
            if (includeBinding.addSupprtingDocCB.isChecked
                && (docType == DocumentType.PDF || docType == DocumentType.IMAGE)
            ) {

                // upload the pdf or the image to the firebase storage

                if (isInternetAvailable(requireContext())) {

                    //uploadDocToFirebaseStorage()

                    val fileNameForUploadingToStorage = if (docType == DocumentType.PDF) {

                        "${includeBinding.fileNameET.text.trim()}_${generateKey()}.pdf"
                    } else {

                        "${includeBinding.fileNameET.text.trim()}_${generateKey()}.jpg"
                    }

                    if (fileNameForUploadingToStorage.contains("/")) {

                        includeBinding.fileNameET.requestFocus()
                        includeBinding.fileNameET.error = "file name should not contain any '/'"
                        return
                    }

                    initBorrowerPayment(fileNameForUploadingToStorage)

                } else {

                    showToast(
                        requireContext(),
                        "Internet connection is required to upload the $docType.",
                        Toast.LENGTH_LONG
                    )

                    return
                }

            } else {

                // insert the data to the database

                initBorrowerPayment("")
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

        if (includeBinding.addSupprtingDocCB.isChecked) {

            if (docType == DocumentType.PDF && pdfUri == null) {

                showToast(
                    requireContext(),
                    "Please add a pdf file as supporting document.",
                    duration = Toast.LENGTH_LONG
                )
                return false
            }

            if (docType == DocumentType.IMAGE && imageUri == null) {

                showToast(
                    requireContext(),
                    "Please add an image as supporting document.",
                    duration = Toast.LENGTH_LONG
                )
                return false
            }

            if (!includeBinding.fileNameET.isTextValid()) {

                includeBinding.fileNameET.requestFocus()
                includeBinding.fileNameET.error = EDIT_TEXT_EMPTY_MESSAGE
                return false
            }

            if (docType == DocumentType.URL && !includeBinding.urlET.isTextValid()) {

                includeBinding.urlET.error = EDIT_TEXT_EMPTY_MESSAGE
                return false
            }
        }

        if (includeBinding.addSupprtingDocCB.isChecked) {

            if (docType != DocumentType.URL) {

                if (fileSize / (1024 * 1024) > 3) {

                    showToast(
                        requireContext(),
                        "Supporting document size should be less than or equal to 3MB",
                        Toast.LENGTH_LONG
                    )

                    return false
                }
            }
        }

        return includeBinding.borrowerPaymentET.editText?.isTextValid()!!
    }

    private fun initBorrowerPayment(fileNameForUploadingToStorage: String) {

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
            isSupportingDocAdded = includeBinding.addSupprtingDocCB.isChecked

            if (isSupportingDocAdded) {

                supportingDocument = SupportingDocument(
                    includeBinding.fileNameET.text.toString().trim(),
                    if (docType == DocumentType.URL) includeBinding.urlET.text.toString()
                        .trim() else "",
                    docType
                )
            }

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

        if (isInternetAvailable(requireContext())) {

            borrowerPayment.isSynced = true

/*
            uploadDocumentToFireStore(
                requireContext(),
                fromBorrowerPaymentToString(borrowerPayment),
                getString(R.string.borrowerPayments),
                borrowerPayment.key
            )
*/

            insertToDatabase(borrowerPayment, fileNameForUploadingToStorage)
        } else {

            borrowerPayment.isSynced = false
            insertToDatabase(borrowerPayment, fileNameForUploadingToStorage)
        }
    }

    private fun insertToDatabase(
        borrowerPayment: BorrowerPayment,
        fileNameForUploadingToStorage: String
    ) {

        Log.d(TAG, "insertToDatabase: ")

        borrowerPaymentViewModel.insertBorrowerPayment(
            borrowerPayment
        )

        if (borrowerPayment.isSupportingDocAdded) {

            if (borrowerPayment.supportingDocument?.documentType != DocumentType.URL) {

                val fileUri =
                    if (borrowerPayment.supportingDocument?.documentType == DocumentType.PDF) pdfUri!! else imageUri!!

                // uploading the pdf or image to the firebase storage as well as the borrower payment to firestore
                // using the service
                uploadFileToFirebaseStorage(
                    context = requireContext(),
                    fileInfo = Pair(fileUri, fileNameForUploadingToStorage),
                    uploadDataInfo = Pair(
                        fromBorrowerPaymentToString(borrowerPayment), // borrower payment as string
                        getString(R.string.borrowerPayments) // collection
                    )
                )
            } else {

                if (borrowerPayment.isSynced) {

                    uploadDocumentToFireStore(
                        requireContext(),
                        fromBorrowerPaymentToString(borrowerPayment),
                        getString(R.string.borrowerPayments),
                        borrowerPayment.key
                    )

                }
            }
        } else {

            if (borrowerPayment.isSynced) {

                uploadDocumentToFireStore(
                    requireContext(),
                    fromBorrowerPaymentToString(borrowerPayment),
                    getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )
            }
        }

        borrowerPaymentViewModel.getTotalDueOfTheBorrower(receivedBorrowerKey).observe(
            viewLifecycleOwner, {

                if (it != null) {

                    receivedBorrower?.totalDueAmount = it
                    receivedBorrower?.modified = System.currentTimeMillis()
                    receivedBorrower?.isSynced = false

                    val map = HashMap<String, Any?>()
                    map["totalDueAmount"] = it

                    if (isInternetAvailable(requireContext())) {

                        receivedBorrower?.isSynced = true

                        updateDocumentOnFireStore(
                            requireContext(),
                            map = map,
                            getString(R.string.borrowers),
                            receivedBorrowerKey
                        )
                    }

                    borrowerViewModel.updateBorrower(receivedBorrower!!)
                }
            }
        )

        lifecycleScope.launch {

            delay(150)

            requireActivity().onBackPressed()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        hideKeyBoard(requireActivity())

        when (buttonView?.id) {

            includeBinding.addInterestCB.id -> showInterestCardView(isChecked)

            includeBinding.addSupprtingDocCB.id -> showAddSupportingDocCardView(isChecked)
        }

    }

    private var recentUrlName = ""

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        hideKeyBoard(requireActivity())

        when (checkedId) {

            includeBinding.pdfRB.id -> {

                docType = DocumentType.PDF
                includeBinding.fileNameET.hint = "Enter file name"
                showFileUploadNoteTV(true)
                showUrlEditText(false)

                if (pdfUri != null) {

                    showFileNameEditText(true)
                    showRemoveFileBtn(true)
                    showAddFileBtn(false)

                    val fileNameAndSize =
                        pdfUri?.getFileNameAndSize(requireActivity().contentResolver)

                    includeBinding.fileNameET
                        .setText(fileNameAndSize?.first)

                    fileSize = fileNameAndSize?.second!!

                } else {

                    showFileNameEditText(false)
                    showRemoveFileBtn(false)
                    showAddFileBtn(true)
                }

            }

            includeBinding.imageRB.id -> {

                includeBinding.fileNameET.hint = "Enter image name"
                docType = DocumentType.IMAGE
                showFileUploadNoteTV(true)
                showUrlEditText(false)

                if (imageUri != null) {

                    showFileNameEditText(true)
                    showRemoveFileBtn(true)
                    showAddFileBtn(false)

                    val fileNameAndSize =
                        imageUri?.getFileNameAndSize(requireActivity().contentResolver)

                    includeBinding.fileNameET
                        .setText(fileNameAndSize?.first)

                    fileSize = fileNameAndSize?.second!!
                } else {

                    showFileNameEditText(false)
                    showRemoveFileBtn(false)
                    showAddFileBtn(true)
                }
            }

            includeBinding.urlRB.id -> {

                includeBinding.fileNameET.hint = "Enter url name here"
                docType = DocumentType.URL

                includeBinding.fileNameET.setText(recentUrlName)

                showUrlEditText(true)
                showFileNameEditText(true)
                showAddFileBtn(false)
                showRemoveFileBtn(false)
                showFileUploadNoteTV(false)
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

            if (docType == DocumentType.PDF) {

                //pdf uri
                pdfUri = uri
            } else {

                // image uri
                imageUri = uri
            }

            val fileNameAndSize = it.getFileNameAndSize(requireActivity().contentResolver)

            if (fileNameAndSize != null) {

                includeBinding.fileNameET.setText(fileNameAndSize.first)
                fileSize = fileNameAndSize.second
            }

            Log.d(
                TAG,
                "FileNameAndSize: ${fileNameAndSize?.first} " +
                        "and size : ${fileNameAndSize?.second?.div(1024 * 1024)}"
            )

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

                if (docType == DocumentType.URL) {

                    recentUrlName = s.toString()
                }
            }
        }

        includeBinding.urlET.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.urlET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.urlET.error = null
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

    private fun showUrlEditText(isVisible: Boolean) {

        includeBinding.urlET.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        includeBinding.urlET.isEnabled = isVisible
    }

    private fun showRemoveFileBtn(isVisible: Boolean) {

        includeBinding.removeFileBtn.isVisible = isVisible
    }

    private fun showFileUploadNoteTV(isVisible: Boolean) {

        includeBinding.uploadingFileNoteTV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }


}
