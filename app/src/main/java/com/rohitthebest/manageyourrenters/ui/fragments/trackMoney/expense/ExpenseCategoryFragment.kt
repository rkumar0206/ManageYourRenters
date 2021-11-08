package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExpenseCategoryFragment : Fragment(R.layout.fragment_expense_category) {

    private var _binding: FragmentExpenseCategoryBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseCategoryBinding.bind(view)

        initListeners()

        binding.progressbar.show()

        lifecycleScope.launch {

            delay(300)
            observeExpenseCategories()
        }

        setUpRecyclerView()

    }

    private fun setUpRecyclerView() {

        binding.expenseCategoryRV.apply {

            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observeExpenseCategories() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner, { expenseCategories ->

                if (expenseCategories.isNotEmpty()) {

                    binding.noExpenseCategoryTV.hide()
                    binding.expenseCategoryRV.show()
                } else {

                    binding.noExpenseCategoryTV.show()
                    binding.expenseCategoryRV.hide()
                }

                binding.progressbar.hide()

                // submit list to adapter
            })
    }

    private fun initListeners() {

        binding.addExpenseCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_expenseCategoryFragment_to_addEditExpenseCategoryFragment)
        }

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
