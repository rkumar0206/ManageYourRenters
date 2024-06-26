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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.ParsedImportExportExpense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentImportBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isPermissionGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.ParsedImportExportExpenseJsonDeserializer
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.downloadFileFromUrl
import com.rohitthebest.manageyourrenters.utils.getFileNameAndSize
import com.rohitthebest.manageyourrenters.utils.hide
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
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private lateinit var allExpenseCategories: List<ExpenseCategory>
    private lateinit var allPaymentMethods: List<PaymentMethod>

    private var previewStringJson = "";
    private lateinit var parsedImportExportExpensesAfterValidation: List<ParsedImportExportExpense>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentImportBinding.bind(view)

        parsedImportExportExpensesAfterValidation = emptyList()

        getExpenseCategoryListAndPaymentMethodList()
        initListeners()
        initUIBeforeSelectingFile()
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.selectFileMCV.setOnClickListener {

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
                importExpenses()
            } else {
                requireContext().showToast("Nothing to import please select a valid csv or json file")
            }
        }

    }

    private fun importExpenses() {

        //todo: using expense category check if the category extracted in file exists or not
        //todo: if category does not exist then create a category and then import expenses
        //todo: Also for payment methods extract the payment methods and check if the payment already exist
        //todo: if not create a payment method and then add it to expense

        parsedImportExportExpensesAfterValidation.forEach { parsedImportExportExpense ->

            val expenseCategory = if (allExpenseCategories.isEmpty()) {

                createNewExpenseCategory(parsedImportExportExpense.category.trim())
            } else {

                val filteredExpenseCategories = allExpenseCategories.filter { category ->
                    category.categoryName == parsedImportExportExpense.category.trim()
                }.stream().findFirst().orElse(null)

                filteredExpenseCategories
                    ?: createNewExpenseCategory(parsedImportExportExpense.category.trim())
            }

            var paymentMethods: List<String>

            if (parsedImportExportExpense.paymentMethod.isValid()) {

                val isMultiplePaymentMethod =
                    parsedImportExportExpense.paymentMethod?.contains("|") ?: false

                if (isMultiplePaymentMethod) {

                    paymentMethods =
                        parsedImportExportExpense.paymentMethod?.split("|") ?: emptyList()

                    if (paymentMethods.isEmpty()) {
                        paymentMethods =
                            parsedImportExportExpense.paymentMethod?.split(" | ") ?: emptyList()
                    }

                } else {

                    paymentMethods = listOf(parsedImportExportExpense.paymentMethod!!)
                }
            } else {
                paymentMethods = listOf(Constants.PAYMENT_METHOD_OTHER_KEY)
            }

            if (allPaymentMethods.isEmpty()) {

                //todo: need to create all payment methods
            } else {

                //todo: filter the payment methods which are already present in db and if not create one

                paymentMethods.forEach { paymentMethod ->

                    //todo: check if payment method is already in
                }
            }


        }


    }

    private fun createNewExpenseCategory(category: String): ExpenseCategory {

        val expenseCategory = ExpenseCategory(
            null,
            category,
            category,
            "",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            Functions.getUid()!!,
            Functions.generateKey("_${Functions.getUid()}", 60),
            true
        )

        expenseCategoryViewModel.insertExpenseCategory(expenseCategory)

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
                    i++;
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

    private fun getExpenseCategoryListAndPaymentMethodList() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { expenseCategories ->
                allExpenseCategories = expenseCategories ?: emptyList()
            }

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->
                allPaymentMethods = paymentMethods ?: emptyList()
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