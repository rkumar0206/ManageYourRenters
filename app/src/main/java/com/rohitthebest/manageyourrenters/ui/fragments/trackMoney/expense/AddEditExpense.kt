package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.AddExpenseLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.isTextValid
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AddEditExpense"

@AndroidEntryPoint
class AddEditExpense : Fragment(R.layout.fragment_add_expense), View.OnClickListener {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddExpenseLayoutBinding

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var receivedExpenseCategoryKey: String
    private lateinit var receivedExpenseCategory: ExpenseCategory

    private lateinit var selectedDate: Calendar

    private var receivedExpenseKey = ""
    private lateinit var receivedExpense: Expense
    private var isMessageReceivedForEditing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddExpenseBinding.bind(view)

        includeBinding = binding.includeLayout

        selectedDate = Calendar.getInstance()

        updateSelectedDateTextView()

        getMessage()

        initListeners()

        textWatchers()

        setUpSpentOnAutoCompleteTextView()
    }

    private fun setUpSpentOnAutoCompleteTextView() {

        expenseViewModel.getAllSpentOn().observe(viewLifecycleOwner) { spentOnList ->

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                spentOnList
            )

            includeBinding.expenseSpentOnET.setAdapter(adapter)
        }
    }

    private fun updateSelectedDateTextView() {

        includeBinding.expenseDateTV.text =
            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                selectedDate.timeInMillis, "dd-MM-yyyy hh:mm a"
            )
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                AddEditExpenseArgs.fromBundle(bundle)
            }

            receivedExpenseCategoryKey = args?.expenseCategoryKey!!

            receivedExpenseKey = args.expenseKey!!

            if (receivedExpenseKey != "") {

                isMessageReceivedForEditing = true

                getExpense()
            }

            lifecycleScope.launch {

                delay(300)
                getExpenseCategory()
            }
        }
    }

    private fun getExpense() {

        expenseViewModel.getExpenseByKey(receivedExpenseKey)
            .observe(viewLifecycleOwner) { expense ->

                receivedExpense = expense

                updateUI()
            }
    }

    private fun updateUI() {

        if (this::receivedExpense.isInitialized) {

            includeBinding.apply {

                selectedDate =
                    WorkingWithDateAndTime.convertMillisecondsToCalendarInstance(receivedExpense.created)
                updateSelectedDateTextView()
                expenseAmountET.editText?.setText(receivedExpense.amount.toString())
                expenseSpentOnET.setText(receivedExpense.spentOn)
            }
        }
    }

    private fun getExpenseCategory() {

        if (this::receivedExpenseCategoryKey.isInitialized) {

            expenseCategoryViewModel.getExpenseCategoryByKey(receivedExpenseCategoryKey)
                .observe(viewLifecycleOwner) { expenseCategory ->

                    receivedExpenseCategory = expenseCategory

                    binding.toolbar.title = "Add ${expenseCategory.categoryName} Expense"
                }
        }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (isFormValid()) {

                initExpense()
            }

            true
        }

        includeBinding.expenseDateTV.setOnClickListener(this)
        includeBinding.expenseDateIB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        if (v?.id == includeBinding.expenseDateTV.id || v?.id == includeBinding.expenseDateIB.id) {


            Functions.showDateAndTimePickerDialog(
                requireContext(),
                selectedDate,
                false
            ) { selectedDate ->

                this.selectedDate = selectedDate
                updateSelectedDateTextView()
            }
        }

    }


    private fun initExpense() {

        val expense: Expense

        if (!isMessageReceivedForEditing) {

            expense = Expense(
                null,
                includeBinding.expenseAmountET.editText?.text.toString().trim().toDouble(),
                selectedDate.timeInMillis,
                System.currentTimeMillis(),
                includeBinding.expenseSpentOnET.text.toString().trim(),
                getUid()!!,
                generateKey("_${getUid()}", 60),
                receivedExpenseCategoryKey,
                true
            )

            saveExpenseInDatabase(expense)

        } else {

            expense = receivedExpense.copy()

            val oldDate = receivedExpense.created
            val oldAmount = receivedExpense.amount
            val oldSpentOn = receivedExpense.spentOn

            Log.d(TAG, "initExpense: oldDate = $oldDate -> newDate = $selectedDate")
            Log.d(
                TAG, "initExpense: oldAmount = $oldAmount -> newAmount = ${
                    includeBinding.expenseAmountET.editText?.text.toString().trim()
                        .toDouble()
                }"
            )
            Log.d(
                TAG,
                "initExpense: oldSpentOn = $oldSpentOn -> newSpentOn = ${
                    includeBinding.expenseSpentOnET.text.toString().trim()
                }"
            )


            if (oldDate != selectedDate.timeInMillis
                || oldAmount != includeBinding.expenseAmountET.editText?.text.toString().trim()
                    .toDouble()
                || oldSpentOn != includeBinding.expenseSpentOnET.text.toString().trim()
            ) {

                expense.created = selectedDate.timeInMillis
                expense.amount =
                    includeBinding.expenseAmountET.editText?.text.toString().trim().toDouble()
                expense.spentOn = includeBinding.expenseSpentOnET.text.toString().trim()

                expense.modified = System.currentTimeMillis()
                expense.isSynced = true
                saveExpenseInDatabase(expense)

            } else {

                showToast(requireContext(), "No change detected...")
                requireActivity().onBackPressed()
            }
        }

    }

    private fun saveExpenseInDatabase(expense: Expense) {

        val oldValue = receivedExpenseCategory.copy()
        receivedExpenseCategory.modified = System.currentTimeMillis()

        expenseCategoryViewModel.updateExpenseCategory(
            oldValue,
            receivedExpenseCategory
        )

        if (!isMessageReceivedForEditing) {

            expenseViewModel.insertExpense(expense)
        } else {

            expenseViewModel.updateExpense(receivedExpense, expense)
        }

        Log.d(TAG, "saveExpenseInDatabase: expense saved -> $expense")

        requireActivity().onBackPressed()
    }

    private fun isFormValid(): Boolean {

        if (!includeBinding.expenseAmountET.editText.isTextValid()) {

            includeBinding.expenseAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return includeBinding.expenseAmountET.error == null
    }

    private fun textWatchers() {

        includeBinding.expenseAmountET.editText?.addTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.expenseAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.expenseAmountET.error = null
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
