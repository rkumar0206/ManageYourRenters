package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.AddExpenseLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.setDateInTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    private var selectedDate = 0L

    private var receivedExpenseKey = ""
    private lateinit var receivedExpense: Expense
    private var isMessageReceivedForEditing = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddExpenseBinding.bind(view)

        includeBinding = binding.includeLayout

        selectedDate = System.currentTimeMillis()

        updateSelectedDateTextView()

        getMessage()

        initListeners()

        textWatchers()
    }

    private fun updateSelectedDateTextView() {

        includeBinding.expenseDateTV.setDateInTextView(
            selectedDate
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
            .observe(viewLifecycleOwner, { expense ->

                receivedExpense = expense

                updateUI()
            })
    }

    private fun updateUI() {

        if (this::receivedExpense.isInitialized) {

            includeBinding.apply {

                selectedDate = receivedExpense.created
                updateSelectedDateTextView()
                expenseAmountET.editText?.setText(receivedExpense.amount.toString())
                expenseSpentOnET.setText(receivedExpense.spentOn)
            }
        }
    }

    private fun getExpenseCategory() {

        if (this::receivedExpenseCategoryKey.isInitialized) {

            expenseCategoryViewModel.getExpenseCategoryByKey(receivedExpenseCategoryKey)
                .observe(viewLifecycleOwner, { expenseCategory ->

                    receivedExpenseCategory = expenseCategory

                    binding.toolbar.title = "Add ${expenseCategory.categoryName} Expenses"
                })
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

            Functions.showCalendarDialog(
                selectedDate,
                {
                    requireActivity().supportFragmentManager
                },
                { selectedDate ->

                    this.selectedDate = selectedDate
                    updateSelectedDateTextView()
                }
            )
        }

    }

    private fun initExpense() {

        val expense: Expense

        if (!isMessageReceivedForEditing) {

            expense = Expense(
                null,
                includeBinding.expenseAmountET.editText?.text.toString().trim().toDouble(),
                selectedDate,
                System.currentTimeMillis(),
                includeBinding.expenseSpentOnET.text.toString().trim(),
                getUid()!!,
                generateKey("_${getUid()}", 60),
                receivedExpenseCategoryKey,
                false
            )

            saveExpenseInDatabase(expense)

        } else {

            expense = receivedExpense

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

            if (oldDate != selectedDate
                || oldAmount != includeBinding.expenseAmountET.editText?.text.toString().trim()
                    .toDouble()
                || oldSpentOn != includeBinding.expenseSpentOnET.text.toString().trim()
            ) {

                expense.created = selectedDate
                expense.amount =
                    includeBinding.expenseAmountET.editText?.text.toString().trim().toDouble()
                expense.spentOn = includeBinding.expenseSpentOnET.text.toString().trim()

                expense.modified = System.currentTimeMillis()

                saveExpenseInDatabase(expense)

            } else {

                Functions.showToast(requireContext(), "No change detected...")
                requireActivity().onBackPressed()
            }
        }

    }

    private fun saveExpenseInDatabase(expense: Expense) {

        if (!isMessageReceivedForEditing) {

            expenseViewModel.insertExpense(requireContext(), expense)
        } else {

            expenseViewModel.updateExpense(requireContext(), expense)
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
