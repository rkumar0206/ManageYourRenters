package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentCategoryBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MonthlyPaymentCategoryFragment : Fragment(R.layout.fragment_monthly_payment_category) {

    private var _binding: FragmentMonthlyPaymentCategoryBinding? = null
    private val binding get() = _binding!!
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private var rvStateParcelable: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentCategoryBinding.bind(view)

        initListeners()
        binding.progressbar.show()
        getMonthlyPaymentCategoryRvState()
        lifecycleScope.launch {
            delay(300)
            observeMonthlyPaymentCategories()
        }
    }

    private fun getMonthlyPaymentCategoryRvState() {

        monthlyPaymentCategoryViewModel.monthlyPaymentCategoryRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }
    }

    private fun observeMonthlyPaymentCategories() {

        monthlyPaymentCategoryViewModel.getAllMonthlyPaymentCategories()
            .observe(viewLifecycleOwner) { categories ->

                // todo : submit the list to the adapter
            }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.addMonthlyPaymentCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_monthlyPaymentCategoryFragment_to_addEditMonthlyPaymentCategory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        monthlyPaymentCategoryViewModel.saveMonthlyPaymentCategoryRvState(
            binding.monthlyPaymentCategoryRV.layoutManager?.onSaveInstanceState()
        )

        _binding = null
    }
}
