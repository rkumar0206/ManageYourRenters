package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.export_import

import android.Manifest
import android.app.Activity.RESULT_OK
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
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentExportImportHomeBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isPermissionGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.getFileNameAndSize
import com.rohitthebest.manageyourrenters.utils.isNotValid
import com.rohitthebest.manageyourrenters.utils.showSnackbarWithActionAndDismissListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

private const val TAG = "ExportImportHomeFragment"

@AndroidEntryPoint
class ExportImportHomeFragment : Fragment(R.layout.fragment_export_import_home) {

    private var _binding: FragmentExportImportHomeBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private lateinit var allExpenseCategories: List<ExpenseCategory>
    private lateinit var allPaymentMethods: List<PaymentMethod>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExportImportHomeBinding.bind(view)

        getExpenseCategoryListAndPaymentMethodList()
        initListeners()
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

    private fun initListeners() {

        binding.exportButton.setOnClickListener {

            //todo: open fragment to choose the categories for exporting
            requireContext().showToast("Export button working")

        }

        binding.importButton.setOnClickListener {

            // todo: open file manager to select the csv file to import

            // check for storage permission

            requireContext().showToast("Import button working")


            if (requireContext().isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                && requireContext().isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)
                || requireContext().isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            ) {

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/csv"
                }
                chooseDocumentLauncher.launch(intent)

            }

        }
    }


    private val chooseDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {

            result?.data?.data?.let {

                val fileNameAndSize = it.getFileNameAndSize(requireActivity().contentResolver)

                Log.d(
                    TAG,
                    "FileNameAndSize: ${fileNameAndSize?.first} " +
                            "and size : ${fileNameAndSize?.second?.div(1024 * 1024)}"
                )

                lifecycleScope.launch(Dispatchers.IO) {
                    readCsvFile(uri = it)
                }

            }
        }


    }

    private suspend fun readCsvFile(uri: Uri) {

        withContext(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))

                var line: String?

                var i = 0;

                while (reader.readLine().also { line = it } != null) {

                    val columnValues = line!!.split(",") // Safe call (elvis) operator

                    // Process each value in the array (values)

                    if (i == 0) {
                        val expectedColumnHeaders =
                            listOf("date", "amount", "category", "spentOn", "paymentMethod")

                        val isAllColumnPresent = expectedColumnHeaders.containsAll(columnValues)

                        if (!isAllColumnPresent) {
                            showToast(
                                requireContext(),
                                "Invalid csv file: Please add all the columns in the csv file",
                                Toast.LENGTH_LONG
                            )
                        }
                    }

                    try {

                        val dateInMillis: Long = validateDateAndConvertItToMillis(columnValues[0])
                        val amount: Double = validateAmount(columnValues[1])
                        val expenseCategory: ExpenseCategory = validateCategory(columnValues[2])
                        val paymentMethods: PaymentMethod = validatePaymentMethod(columnValues[4])


                    } catch (e: Exception) {
                        showToast(requireContext(), e.message, Toast.LENGTH_LONG)
                    }

                    i++;
                }

                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun validatePaymentMethod(paymentMethod: String): PaymentMethod {

        // todo: also check for '|' character and if exist it means it contains multiple payment methods
        //todo:  search for payment method in database
        // todo: if found return it
        // todo: if not found create a new payment method with that name

        val isMultiplePaymentMethod = paymentMethod.contains("|")

        if (isMultiplePaymentMethod) {

        } else {

        }
        return PaymentMethod()
    }

    private fun validateCategory(categoryName: String): ExpenseCategory {

        // search category in database
        // if found return the expense category
        // if not found create a new expense category with that name

        if (categoryName.isNotValid()) {
            throw IllegalArgumentException("Invalid category")
        }

        return if (allExpenseCategories.isEmpty()) {

            createNewExpenseCategory(categoryName)
        } else {

            val filteredExpenseCategories = allExpenseCategories.filter { expenseCategory ->
                expenseCategory.categoryName == categoryName.trim()
            }

            if (filteredExpenseCategories.isEmpty()) {
                createNewExpenseCategory(categoryName)
            } else {
                filteredExpenseCategories[0]
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

    private fun validateDateAndConvertItToMillis(date: String): Long {

        // check if the date is in date format or not
        // if not in date format then return current date and time
        // if date is null or empty then return current date and time

        if (date.isNotValid()) return System.currentTimeMillis()

        return try {
            WorkingWithDateAndTime.identifyDateAndTimeFormatAndConvertToMillis(date)
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}