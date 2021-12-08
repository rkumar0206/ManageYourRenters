package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.DeepAnalyzeExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentDeepAnalyzeExpenseBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeepAnalyzeExpenseFragment : Fragment(R.layout.fragment_deep_analyze_expense),
    DeepAnalyzeExpenseCategoryAdapter.OnClickListener {

    private var _binding: FragmentDeepAnalyzeExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()


    private lateinit var deepAnalyzeExpenseCategoryAdapter: DeepAnalyzeExpenseCategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeepAnalyzeExpenseBinding.bind(view)

        deepAnalyzeExpenseCategoryAdapter = DeepAnalyzeExpenseCategoryAdapter()

        setUpRecyclerView()

        getAllExpenseCategories()
    }

    private fun getAllExpenseCategories() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { expenseCategories ->

                deepAnalyzeExpenseCategoryAdapter.submitList(expenseCategories)
            }
    }

    private fun setUpRecyclerView() {

        binding.expenseCategoriesRv.apply {

            setHasFixedSize(true)
            adapter = deepAnalyzeExpenseCategoryAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }

        deepAnalyzeExpenseCategoryAdapter.setOnClickListener(this)
    }

    override fun onItemClick(expenseCategory: ExpenseCategory, position: Int) {

        expenseCategory.isSelected = !expenseCategory.isSelected
        deepAnalyzeExpenseCategoryAdapter.notifyItemChanged(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
