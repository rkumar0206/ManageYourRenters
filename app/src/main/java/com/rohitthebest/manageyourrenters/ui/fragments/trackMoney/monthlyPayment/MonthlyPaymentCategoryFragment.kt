package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters.MonthlyPaymentCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentCategoryBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfFABOnScrolled
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.searchText
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MonthlyPaymentCategoryFragment : Fragment(R.layout.fragment_monthly_payment_category),
    MonthlyPaymentCategoryAdapter.OnClickListener {

    private var _binding: FragmentMonthlyPaymentCategoryBinding? = null
    private val binding get() = _binding!!
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private var rvStateParcelable: Parcelable? = null

    private lateinit var monthlyPaymentCategoryAdapter: MonthlyPaymentCategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentCategoryBinding.bind(view)

        monthlyPaymentCategoryAdapter = MonthlyPaymentCategoryAdapter()

        initListeners()
        binding.progressbar.show()
        getMonthlyPaymentCategoryRvState()
        lifecycleScope.launch {
            delay(300)
            observeMonthlyPaymentCategories()
        }
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        binding.monthlyPaymentCategoryRV.apply {

            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = monthlyPaymentCategoryAdapter
            changeVisibilityOfFABOnScrolled(binding.addMonthlyPaymentCategoryFAB)
        }

        monthlyPaymentCategoryAdapter.setOnClickListener(this)
    }


    override fun onItemClick(monthlyPaymentCategory: MonthlyPaymentCategory) {


    }

    override fun onMenuBtnClicked(monthlyPaymentCategory: MonthlyPaymentCategory) {

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

                if (categories.isNotEmpty()) {

                    binding.noMonthlyPaymentCategoryTV.hide()
                    binding.monthlyPaymentCategoryRV.show()
                    setUpSearchView(categories)
                } else {

                    binding.noMonthlyPaymentCategoryTV.show()
                    binding.monthlyPaymentCategoryRV.hide()
                }

                binding.progressbar.hide()

                monthlyPaymentCategoryAdapter.submitList(categories)

                binding.monthlyPaymentCategoryRV.layoutManager?.onRestoreInstanceState(
                    rvStateParcelable
                )
            }
    }

    private fun setUpSearchView(monthlyPaymentCategories: List<MonthlyPaymentCategory>?) {

        val searchView =
            binding.toolbar.menu.findItem(R.id.menu_search_monthly_payment_category).actionView as SearchView

        searchView.clearFocus()
        searchView.setQuery("", true)

        searchView.searchText { s ->

            if (s?.isEmpty()!!) {

                binding.monthlyPaymentCategoryRV.scrollToPosition(0)
                monthlyPaymentCategoryAdapter.submitList(monthlyPaymentCategories)
            } else {

                val filteredList = monthlyPaymentCategories?.filter { monthlyPaymentCategory ->

                    monthlyPaymentCategory.categoryName.lowercase(Locale.ROOT).contains(
                        s.toString().trim().lowercase(Locale.ROOT)
                    )
                }

                monthlyPaymentCategoryAdapter.submitList(filteredList)
            }
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
