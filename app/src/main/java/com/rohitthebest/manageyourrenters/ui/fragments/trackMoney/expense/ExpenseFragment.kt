package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExpenseFragment : Fragment(R.layout.fragment_expense) {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel by viewModels<ExpenseViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseBinding.bind(view)


        binding.progressbar.show()

        lifecycleScope.launch {

            delay(300)
            getALlExpenses()
        }

    }

    private fun getALlExpenses() {


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

