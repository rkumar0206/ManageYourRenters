package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseCategoryFragment : Fragment(R.layout.fragment_expense_category) {

    private var _binding: FragmentExpenseCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseCategoryBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
