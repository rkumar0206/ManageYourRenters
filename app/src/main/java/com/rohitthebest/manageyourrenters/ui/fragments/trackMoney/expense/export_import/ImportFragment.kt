package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.export_import

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.ImportLimitPerUser
import com.rohitthebest.manageyourrenters.data.ImportServiceHelperModel
import com.rohitthebest.manageyourrenters.data.ParsedImportExportExpense
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentImportBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.IMPORT_LIMIT_PER_MONTH
import com.rohitthebest.manageyourrenters.others.Constants.MAXIMUM_BATCH_SIZE
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.services.ImportService
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ImportViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isPermissionGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.ParsedImportExportExpenseJsonDeserializer
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.downloadFileFromUrl
import com.rohitthebest.manageyourrenters.utils.getDocumentFromFirestore
import com.rohitthebest.manageyourrenters.utils.getFileNameAndSize
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.insertToFireStore
import com.rohitthebest.manageyourrenters.utils.isNotValid
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show
import com.rohitthebest.manageyourrenters.utils.showSnackbarWithActionAndDismissListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

private const val TAG = "ImportFragment"

@AndroidEntryPoint
class ImportFragment : Fragment(R.layout.fragment_import) {

    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()
    private val importViewModel by viewModels<ImportViewModel>()

    private lateinit var allExpenseCategories: MutableList<ExpenseCategory>
    private lateinit var allPaymentMethods: MutableList<PaymentMethod>

    private var previewStringJson = ""
    private lateinit var parsedImportExportExpensesAfterValidation: List<ParsedImportExportExpense>
    private var importLimitForUser = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportBinding.bind(view)

        parsedImportExportExpensesAfterValidation = emptyList()

        getExpenseCategoryListAndPaymentMethodList()
        initListeners()
        initUIBeforeSelectingFile()

        observeImportLimit()

        getUserImportLimit()
    }

    private fun observeImportLimit() {

        importViewModel.importLimit.observe(viewLifecycleOwner) { importLimit ->
            importLimitForUser = importLimit
        }
    }

    private fun getUserImportLimit() {

        lifecycleScope.launch {

            getDocumentFromFirestore(
                FirestoreCollectionsConstants.IMPORT_LIMIT,
                getUid()!!,
                {
                    it?.toObject(ImportLimitPerUser::class.java)?.let { importLimit ->

                        val numberOfDays = WorkingWithDateAndTime.calculateNumberOfDays(
                            importLimit.modified, System.currentTimeMillis()
                        )

                        Log.d(
                            TAG,
                            "getUserImportLimit: Number of days since last modified: $numberOfDays"
                        )

                        val isLastModifiedGreaterThanOnMonth = numberOfDays >= 30

                        importLimitForUser =
                            if (isLastModifiedGreaterThanOnMonth) 0 else importLimit.limit

                        importViewModel.updateImportLimit(importLimitForUser)

                        checkForImportLimitAndShowProperMessage()
                    }
                },
                {

                    Log.d(TAG, "getUserImportLimit: Exception occurred\n ${it.message}")
                    importLimitForUser = 0
                }
            )

        }
    }

    private fun checkForImportLimitAndShowProperMessage(): Boolean {

        if (importLimitForUser >= IMPORT_LIMIT_PER_MONTH) {

            showToast(requireContext(), "You have reached your import limit for this month.")
            return false
        }

        return true
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.selectFileMCV.setOnClickListener {

            if (checkForImportLimitAndShowProperMessage()) {

                if (requireContext().isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && requireContext().isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)
                    || requireContext().isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
                ) {

                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "application/json"))
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    chooseDocumentLauncher.launch(intent)

                } else {

                    requestPermission()
                }
            }

        }

        binding.downloadSampleCsvMCV.setOnClickListener {

            showDialogAndDownloadFile(
                "https://firebasestorage.googleapis.com/v0/b/manageyourrenters.appspot.com/o/app_internal%2Fsample_expense_csv.csv?alt=media&token=6a5dde0c-b0cc-45ec-935a-802481880c56",
                "sample_file_csv.csv"
            )
        }

        binding.downloadSampleJsonMCV.setOnClickListener {

            showDialogAndDownloadFile(
                "https://firebasestorage.googleapis.com/v0/b/manageyourrenters.appspot.com/o/app_internal%2Fsample_expense_json.json?alt=media&token=358428f5-40f2-4892-b1cc-1ca01917aa16",
                "sample_file_json.json"
            )
        }

        binding.removeOpenedFileIB.setOnClickListener {

            initUIBeforeSelectingFile()
        }

        binding.importOpenedFileBtn.setOnClickListener {

            if (parsedImportExportExpensesAfterValidation.isNotEmpty()) {
                if (checkForImportLimitAndShowProperMessage()) {
                    importExpenses()
                }
            } else {
                requireContext().showToast("Nothing to import please select a valid csv or json file")
            }
        }

    }

    private fun handleCSVFile(uri: Uri) {

        val fileNameAndSize = uri.getFileNameAndSize(requireActivity().contentResolver)

        Log.d(
            TAG,
            "FileName: ${fileNameAndSize?.first} " +
                    "and size : ${fileNameAndSize?.second?.div(1024 * 1024)}"
        )

        binding.openedFileNameTV.text = fileNameAndSize?.first

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                val parsedImportExportExpenses = validateCSVFileAndGetData(uri)

                if (!parsedImportExportExpenses.first) {

                    withContext(Dispatchers.Main) {
                        showToast(requireContext(), "Invalid CSV file", Toast.LENGTH_LONG)
                        initUIBeforeSelectingFile()
                    }
                } else {

                    withContext(Dispatchers.Main) {

                        if (parsedImportExportExpenses.third.toString().isValid()) {

                            //todo: show the dialog or think how to show the error messages
                        }

                        initUIAfterSelectingFile()

                        parsedImportExportExpensesAfterValidation =
                            parsedImportExportExpenses.second

                        val gson = Gson().newBuilder().setPrettyPrinting().create()
                        previewStringJson = gson.toJson(parsedImportExportExpenses.second)

                        Log.d(TAG, "handleCSVFile: previewString: $previewStringJson")
                        binding.jsonTextView.text = previewStringJson
                    }
                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    showToast(requireContext(), e.message, Toast.LENGTH_LONG)
                    initUIBeforeSelectingFile()
                }
            }
        }
    }

    private fun handleJSONFile(uri: Uri) {

        val fileNameAndSize = uri.getFileNameAndSize(requireActivity().contentResolver)

        Log.d(
            TAG,
            "FileNameAndSize: ${fileNameAndSize?.first} " +
                    "and size : ${fileNameAndSize?.second?.div(1024 * 1024)}"
        )

        binding.openedFileNameTV.text = fileNameAndSize?.first

        lifecycleScope.launch {

            try {

                val parsedImportExportExpenses = validateJSONFileAndGetData(uri)

                withContext(Dispatchers.Main) {


                    if (parsedImportExportExpenses.second.toString().isValid()) {

                        //todo: show the dialog or think how to show the error messages
                    }

                    initUIAfterSelectingFile()

                    parsedImportExportExpensesAfterValidation = parsedImportExportExpenses.first

                    val gson = Gson().newBuilder().setPrettyPrinting().create()
                    previewStringJson = gson.toJson(parsedImportExportExpenses.first)

                    Log.d(TAG, "handleCSVFile: previewString: $previewStringJson")
                    binding.jsonTextView.text = previewStringJson
                }
            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    showToast(requireContext(), e.message, Toast.LENGTH_LONG)
                    initUIBeforeSelectingFile()
                }
            }
        }
    }

    /**
     * returns Pair of
     * 1. List of ParsedImportExportExpense
     * 2. StringBuilder of errorMessages
     */

    private suspend fun validateJSONFileAndGetData(uri: Uri): Pair<List<ParsedImportExportExpense>, StringBuilder> {

        return withContext(Dispatchers.IO) {

            var parsedImportExportExpenses: List<ParsedImportExportExpense> = emptyList()
            val errorMessages: StringBuilder = StringBuilder()

            try {

                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))

                val jsonText = reader.use { it.readText() }
                Log.d(TAG, "validateJSONFileAndGetData: jsonText $jsonText")

                val gson = GsonBuilder()
                    .registerTypeAdapter(
                        ParsedImportExportExpense::class.java,
                        ParsedImportExportExpenseJsonDeserializer()
                    )
                    .create()

                parsedImportExportExpenses = gson.fromJson(
                    jsonText.trimIndent(),
                    object : TypeToken<List<ParsedImportExportExpense>>() {}.type
                )

                parsedImportExportExpenses.forEach { parsedImportExportExpense ->

                    if (parsedImportExportExpense.category.isNotValid()) {
                        errorMessages.append("Invalid Category for below entry")
                            .append("\n").append(parsedImportExportExpense)
                            .append("\n")
                    }
                }

            } catch (e: Exception) {
                errorMessages.append(e.message).append("\n")
            }

            return@withContext Pair(parsedImportExportExpenses, errorMessages)
        }
    }

    // checks if all the columns are present and proper values are present
    /**
     * returns
     * 1. isAllColumnPresent  - Boolean
     * 2. parsedImportExportExpense - ParsedImportExportExpense
     * 3. errorMessages - StringBuilder
     */
    private suspend fun validateCSVFileAndGetData(uri: Uri): Triple<Boolean, List<ParsedImportExportExpense>, StringBuilder> {

        return withContext(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))

                var line: String?

                var isAllColumnPresent = false

                val parsedImportExportExpense: ArrayList<ParsedImportExportExpense> = ArrayList()

                val errorMessages = StringBuilder()

                var i = 0

                while (reader.readLine().also { line = it } != null) {

                    val columnValues = line!!.split(",")

                    Log.d(TAG, "validateCSVFileAndGetData: CSV column: $columnValues")

                    if (i == 0) {
                        val expectedColumnHeaders =
                            listOf("date", "amount", "category", "spentOn", "paymentMethod")

                        isAllColumnPresent = columnValues == expectedColumnHeaders

                        if (!isAllColumnPresent) {

                            Log.d(
                                TAG,
                                "validateCSVFileAndGetData: Invalid CSV file: Please add all the columns in the csv file"
                            )
                            throw Exception(
                                "Invalid CSV file: Please add all the columns in the csv file"
                            )
                        }
                    } else {

                        try {

                            val dateInMillis: String? =
                                validateDateAndGiveACommonFormat(columnValues[0])
                            val amount: Double = validateAmount(columnValues[1])
                            val expenseCategory =
                                if (columnValues[2].isValid()) columnValues[2] else throw Exception(
                                    "Invalid category: ${columnValues[2]}"
                                )
                            val spentOn = columnValues[3]
                            val paymentMethods = columnValues[4]

                            parsedImportExportExpense.add(
                                ParsedImportExportExpense(
                                    date = dateInMillis,
                                    amount = amount,
                                    category = expenseCategory,
                                    spentOn = spentOn,
                                    paymentMethod = paymentMethods
                                )
                            )

                        } catch (e: Exception) {
                            Log.d(TAG, "validateCSVFileAndGetData: exception: " + e.message)
                            errorMessages.append("Line: $i").append(e.message).append("\n\n")
                        }
                    }
                    i++
                }

                reader.close()

                return@withContext Triple(
                    isAllColumnPresent,
                    parsedImportExportExpense,
                    errorMessages
                )

            } catch (e: Exception) {
                e.printStackTrace()

                throw e
            }
        }

    }

    private fun validateAmount(amount: String): Double {

        // check if the amount is in number format or not
        // if not in number format return 0.0
        // if amount is null or empty return 0.0

        return if (amount.isNotValid()) {
            0.0
        } else {
            try {
                amount.toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        }

    }

    private fun validateDateAndGiveACommonFormat(dateString: String): String? {

        val timeInMillis =
            if (dateString.isNotValid()) System.currentTimeMillis() else WorkingWithDateAndTime.identifyDateAndTimeFormatAndConvertToMillis(
                dateString
            )
        return WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
            timeInMillis,
            "dd-MM-yyyy hh:mm a"
        )
    }

    private fun showDialogAndDownloadFile(url: String, fileName: String) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.download_sample_file))
            .setMessage(getString(R.string.sample_file_will_be_download_in_your_phones_download_directory))
            .setPositiveButton(getString(R.string.download)) { dialog, _ ->

                downloadFileFromUrl(
                    requireActivity(),
                    url,
                    fileName
                )

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()

    }

    private fun importExpenses() {

        val allNonExistingCategoryDocuments = HashMap<String, ExpenseCategory>()
        val allNonExistingPaymentMethodDocuments = HashMap<String, PaymentMethod>()
        val allExpensesDocuments = ArrayList<Expense>()

        parsedImportExportExpensesAfterValidation.forEach { parsedImportExportExpense ->

            // initializing expense category
            val expenseCategory = if (allExpenseCategories.isEmpty()) {

                val newExpenseCategory =
                    createNewExpenseCategory(parsedImportExportExpense.category.trim())

                allNonExistingCategoryDocuments[parsedImportExportExpense.category.trim()] =
                    newExpenseCategory

                allExpenseCategories.add(newExpenseCategory)

                newExpenseCategory
            } else {

                var expenseCategory = allExpenseCategories.filter { category ->
                    category.categoryName == parsedImportExportExpense.category.trim()
                }.stream().findFirst().orElse(null)

                if (expenseCategory == null) {

                    expenseCategory =
                        createNewExpenseCategory(parsedImportExportExpense.category.trim())

                    allNonExistingCategoryDocuments[parsedImportExportExpense.category.trim()] =
                        expenseCategory

                    allExpenseCategories.add(expenseCategory)
                }

                expenseCategory
            }

            // initializing payment methods
            val selectedPaymentMethodKeys = getAllPaymentMethodKeys(
                parsedImportExportExpense.paymentMethod,
                allNonExistingPaymentMethodDocuments
            )

            val dateInMillis =
                WorkingWithDateAndTime.getTimeInMillisFromDateInString(
                    parsedImportExportExpense.date!!,
                    "dd-MM-yyyy hh:mm a"
                )

            // initializing expense
            val expense = Expense(
                key = Functions.generateKey("_${getUid()}"),
                amount = parsedImportExportExpense.amount,
                id = null,
                created = dateInMillis ?: System.currentTimeMillis(),
                modified = dateInMillis ?: System.currentTimeMillis(),
                spentOn = parsedImportExportExpense.spentOn ?: "",
                uid = getUid()!!,
                categoryKey = expenseCategory.key,
                paymentMethods = selectedPaymentMethodKeys,
                isSynced = true
            )

            //expenseViewModel.insertExpense(expense)
            allExpensesDocuments.add(expense)
        }

        Log.d(TAG, "importExpenses: All Non existing categories: $allNonExistingCategoryDocuments")
        Log.d(
            TAG,
            "importExpenses: All Non existing paymentMethods: $allNonExistingPaymentMethodDocuments"
        )
        Log.d(TAG, "importExpenses: All expenses: $allExpensesDocuments")

        val documentSize =
            allNonExistingCategoryDocuments.size + allNonExistingPaymentMethodDocuments.size + allExpensesDocuments.size
        val isSizeValidForBatch = documentSize < MAXIMUM_BATCH_SIZE

        if (isSizeValidForBatch) {

            val newLimit = importLimitForUser + documentSize

            if (newLimit >= IMPORT_LIMIT_PER_MONTH) {

                requireContext().showToast("Import limit exceeded by ${IMPORT_LIMIT_PER_MONTH - (newLimit + 1)}")
            } else {

                if (isInternetAvailable(requireContext())) {

                    callImportServiceWithValidData(
                        allNonExistingCategoryDocuments,
                        allNonExistingPaymentMethodDocuments,
                        allExpensesDocuments
                    )

                    // --------- Inserting import limit to FireStore ---------
                    val docRef = FirebaseFirestore.getInstance()
                        .collection(FirestoreCollectionsConstants.IMPORT_LIMIT)
                        .document(getUid()!!)

                    importLimitForUser = newLimit
                    importViewModel.updateImportLimit(importLimitForUser)

                    lifecycleScope.launch {
                        insertToFireStore(
                            docRef,
                            ImportLimitPerUser(
                                getUid()!!,
                                importLimitForUser,
                                System.currentTimeMillis()
                            )
                        )
                    }
                    // ----------------------------------------------------

                    requireContext().showToast("Importing expenses. See Notification")

                    initUIBeforeSelectingFile()
                } else {
                    showNoInternetMessage(requireContext())
                }
            }
        } else {

            requireContext().showToast(
                "Batch size exceeded. Please try again with less number of expenses, new categories or new payment Methods in csv or json",
                Toast.LENGTH_LONG
            )
        }


    }

    private fun callImportServiceWithValidData(
        allNonExistingCategoryDocuments: java.util.HashMap<String, ExpenseCategory>,
        allNonExistingPaymentMethodDocuments: java.util.HashMap<String, PaymentMethod>,
        allExpensesDocuments: java.util.ArrayList<Expense>
    ) {

        val importServiceHelperModels = ArrayList<ImportServiceHelperModel>()

        importServiceHelperModels.add(
            ImportServiceHelperModel(
                collectionKey = FirestoreCollectionsConstants.EXPENSE_CATEGORIES,
                documents = allNonExistingCategoryDocuments.values.associateBy { it.key } // associating all the categories with their key
            )
        )

        importServiceHelperModels.add(
            ImportServiceHelperModel(
                collectionKey = FirestoreCollectionsConstants.PAYMENT_METHODS,
                documents = allNonExistingPaymentMethodDocuments.values.associateBy { it.key } // associating all the payment methods with their key
            )
        )

        importServiceHelperModels.add(
            ImportServiceHelperModel(
                collectionKey = FirestoreCollectionsConstants.EXPENSES,
                documents = allExpensesDocuments.associateBy { it.key } // associating all the expenses with their key
            )
        )

        val foregroundService = Intent(context, ImportService::class.java)

        foregroundService.putExtra(
            Constants.UPLOAD_DATA_KEY,
            importServiceHelperModels.convertToJsonString()
        )

        ContextCompat.startForegroundService(requireContext(), foregroundService)

    }

    private fun getAllPaymentMethodKeys(
        paymentMethodString: String?,
        allNonExistingPaymentMethodDocuments: HashMap<String, PaymentMethod>
    ): List<String> {

        val paymentMethods: List<String> = getValidPaymentMethods(paymentMethodString)

        val selectedPaymentMethodKeys: ArrayList<String> = ArrayList()

        if (allPaymentMethods.isEmpty()) {

            // as no payment method exists all payment methods need to be create

            paymentMethods.forEach { paymentMethod ->

                val newPaymentMethod = createNewPaymentMethod(paymentMethod.trim())
                allNonExistingPaymentMethodDocuments[paymentMethod.trim()] = newPaymentMethod

                selectedPaymentMethodKeys.add(newPaymentMethod.key)
                allPaymentMethods.add(newPaymentMethod)
            }

        } else {

            //filter the payment methods which are already present in db and if not creating one

            val allPaymentMethodsName = allPaymentMethods.map { it.paymentMethod }

            paymentMethods.forEach { paymentMethod ->

                if (allPaymentMethodsName.contains(paymentMethod.trim())) {
                    selectedPaymentMethodKeys.add(allPaymentMethods.first { it.paymentMethod == paymentMethod.trim() }.key)
                } else {
                    val newPaymentMethod = createNewPaymentMethod(paymentMethod.trim())
                    allNonExistingPaymentMethodDocuments[paymentMethod.trim()] = newPaymentMethod

                    selectedPaymentMethodKeys.add(newPaymentMethod.key)
                    allPaymentMethods.add(newPaymentMethod)
                }
            }
        }

        return selectedPaymentMethodKeys
    }

    private fun getValidPaymentMethods(paymentMethodString: String?): List<String> {

        var paymentMethods = emptyList<String>()

        if (paymentMethodString.isValid()) {

            val isMultiplePaymentMethod =
                paymentMethodString?.contains("|") ?: false

            if (isMultiplePaymentMethod) {

                paymentMethods = paymentMethodString?.split(" | ") ?: emptyList()

                if (paymentMethods.isEmpty()) {
                    paymentMethods = paymentMethodString?.split("|") ?: emptyList()
                }

            } else {

                paymentMethods = listOf(paymentMethodString!!)
            }
        }

        return paymentMethods
    }

    private fun createNewPaymentMethod(paymentMethodString: String): PaymentMethod {

        return PaymentMethod(
            key = Functions.generateKey("_${getUid()}"),
            paymentMethod = paymentMethodString,
            uid = getUid()!!,
            isSynced = isInternetAvailable(requireContext()),
            isSelected = false
        )
    }

    private fun createNewExpenseCategory(category: String): ExpenseCategory {

        val expenseCategory = ExpenseCategory(
            null,
            category,
            category,
            "",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            getUid()!!,
            Functions.generateKey("_${getUid()}", 60),
            true
        )

        //expenseCategoryViewModel.insertExpenseCategory(expenseCategory)

        Log.i(TAG, "saveToDatabase: $expenseCategory")

        return expenseCategory
    }


    private val chooseDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            result?.data?.data?.let { uri ->

                when (requireContext().contentResolver.getType(uri)) {
                    "text/csv" -> {
                        // Handle CSV file
                        handleCSVFile(uri)
                    }

                    "application/json" -> {
                        // Handle JSON file
                        handleJSONFile(uri)
                    }

                    else -> {

                        requireContext().showToast("Unsupported file type", Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }

    private fun getExpenseCategoryListAndPaymentMethodList() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { expenseCategories ->
                allExpenseCategories = expenseCategories.toMutableList()
            }

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->
                allPaymentMethods = paymentMethods.toMutableList()
            }
    }

    private fun requestPermission() {

        when {

            //check if permission already granted
            (requireContext().isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && requireContext().isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)) -> {
                //permission is granted
                return
            }

            // if the app deems that they should show the request permission rationale
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

                binding.root.showSnackbarWithActionAndDismissListener(
                    "Permission is required for selecting file from your storage.",
                    "Ok",
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            requestPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_MEDIA_IMAGES
                                )
                            )
                        } else {
                            requestPermissionLauncher.launch(
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            )
                        }
                    },
                    {
                        //null
                    }
                )
            }

            // request for permission
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    )
                } else {
                    requestPermissionLauncher.launch(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    )
                }
            }
        }
    }

    //[START OF LAUNCHERS]
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->

        isGranted.forEach { (t, u) ->
            if (u) {
                Log.i(TAG, "Permission granted for $t")
            } else {
                Log.i(TAG, "Permission denied for $t")
            }
        }

    }

    private fun initUIBeforeSelectingFile() {

        binding.fileInfoLL.hide()
        binding.actionButtonForImportLL.hide()
        binding.previewSV.hide()
        binding.expenseImportMenusLL.show()
    }

    private fun initUIAfterSelectingFile() {

        binding.fileInfoLL.show()
        binding.actionButtonForImportLL.show()
        binding.previewSV.show()
        binding.expenseImportMenusLL.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}