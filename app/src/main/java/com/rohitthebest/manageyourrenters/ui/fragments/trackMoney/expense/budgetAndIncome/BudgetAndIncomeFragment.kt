package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetAndIncomeFragment : Fragment(R.layout.fragment_budget) {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetBinding.bind(view)

        initUI()
        initListeners()
        //todo: get all expense categories
        // todo: create a budget recyclerview adapter
        // todo: pass all the expense categories
        // todo: in the layout add the button for adding budget to each category
        // todo: modify the expense category table and add a budget field
    }

    private fun initListeners() {
        // todo: date textview should be clickable
        // todo: and when clicked it should show the option like monthly, weekly, and daily
        // todo: the previous and next button should show the next date and previous date
        // todo:
    }

    private fun initUI() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
