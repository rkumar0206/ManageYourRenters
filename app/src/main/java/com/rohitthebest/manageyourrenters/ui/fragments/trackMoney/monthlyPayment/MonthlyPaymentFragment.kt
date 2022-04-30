package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MonthlyPaymentFragment : Fragment(R.layout.fragment_monthly_payment) {

    private var _binding: FragmentMonthlyPaymentBinding? = null
    private val binding get() = _binding!!

    private val monthlyPaymentViewModel by viewModels<MonthlyPaymentViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private lateinit var receivedMonthlyPaymentCategoryKey: String
    private lateinit var receivedMonthlyPaymentCategory: MonthlyPaymentCategory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentBinding.bind(view)

        binding.progressbar.show()
        getMessage()

        initListeners()

    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.addMonthlyPaymentsFAB.setOnClickListener {

            if (receivedMonthlyPaymentCategoryKey.isValid()) {

                val action =
                    MonthlyPaymentFragmentDirections.actionMonthlyPaymentFragmentToAddEditMonthlyPaymentFragment(
                        receivedMonthlyPaymentCategoryKey, ""
                    )

                findNavController().navigate(action)
            } else {

                Functions.showToast(requireContext(), "No category chosen!!!")
            }
        }

    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                MonthlyPaymentFragmentArgs.fromBundle(bundle)
            }

            receivedMonthlyPaymentCategoryKey = args?.monthlyPaymentCategoryKey!!

            lifecycleScope.launch {

                delay(300)
                getMonthlyPaymentCategory()
            }

        }
    }

    private fun getMonthlyPaymentCategory() {

        monthlyPaymentCategoryViewModel.getMonthlyPaymentCategoryUsingKey(
            receivedMonthlyPaymentCategoryKey
        )
            .observe(viewLifecycleOwner) { category ->

                receivedMonthlyPaymentCategory = category
                binding.toolbar.title = "${category.categoryName} payments"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
