package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.data.ShowExpenseBottomSheetTagsEnum
import com.rohitthebest.manageyourrenters.data.filter.ExpenseFilterDto
import com.rohitthebest.manageyourrenters.databinding.FragmentGraphBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY
import com.rohitthebest.manageyourrenters.others.Constants.CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME
import com.rohitthebest.manageyourrenters.others.Constants.ONE_DAY_MILLISECONDS
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.ShowPaymentMethodSelectorDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseGraphDataViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBitmapToCacheDirectoryAndShare
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "GraphFragment"

@AndroidEntryPoint
class GraphFragment : Fragment(R.layout.fragment_graph),
    ShowPaymentMethodSelectorDialogFragment.OnClickListener {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseGraphDataViewModel by viewModels<ExpenseGraphDataViewModel>()

    private lateinit var pie: Pie
    private var isAllTimeSelected = false
    private var d1 = System.currentTimeMillis() - (ONE_DAY_MILLISECONDS * 30)
    private var d2 = System.currentTimeMillis()

    private var selectedCustomDateRangeMenu: CustomDateRange? = CustomDateRange.ALL_TIME

    private var expenseFilterDto: ExpenseFilterDto? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphBinding.bind(view)

        //setUpCartesianChart()
        changeSelectionUI()
        setUpPieChart()

        loadCustomDateRangeValueFromSharedPreference()

        handleDateRangeSelectionMenu(selectedCustomDateRangeMenu ?: CustomDateRange.ALL_TIME)
        initListeners()

        observeExpenseGraphData()
    }

    private fun observeExpenseGraphData() {

        expenseGraphDataViewModel.expenseGraphData.observe(viewLifecycleOwner) {

            if (it != null && it.first.isNotEmpty()) {

                val expenseCategoryNameAndTheirTotalList = it.first
                val total = it.second

                pie.legend().title(getString(R.string.total_expense, total.format(3)))

                val data = ArrayList<DataEntry>()

                expenseCategoryNameAndTheirTotalList.forEach { expenseCategoryAndTheirTotalExpenseAmounts ->

                    data.add(
                        ValueDataEntry(
                            expenseCategoryAndTheirTotalExpenseAmounts.categoryName,
                            expenseCategoryAndTheirTotalExpenseAmounts.totalAmount
                        )
                    )
                }

                lifecycleScope.launch {

                    delay(500)

                    Log.d(TAG, "getAllExpenseCategory: ${data.size}")

                    if (data.isNotEmpty()) {

                        binding.chart.show()
                        binding.noDataTV.hide()
                        pie.data(data)
                    } else {

                        handleUIForNoDataAvailable()
                    }
                }
            } else {
                handleUIForNoDataAvailable()
            }
        }
    }

    private fun handleUIForNoDataAvailable() {

        binding.chart.hide()
        binding.noDataTV.show()
        showToast(requireContext(), getString(R.string.no_data_available))
    }

    private fun loadCustomDateRangeValueFromSharedPreference() {

        selectedCustomDateRangeMenu =
            requireActivity().loadAnyValueFromSharedPreference(
                CustomDateRange::class.java,
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME,
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY
            )

        if (selectedCustomDateRangeMenu == null) {

            selectedCustomDateRangeMenu = CustomDateRange.ALL_TIME
        }

    }


    private fun setUpPieChart() {

        Log.d(TAG, "setUpPieChart: ")

        pie = AnyChart.pie()

        binding.chart.setProgressBar(binding.progressBar)

        pie.title().enabled(true)
        pie.title(getString(R.string.all_time))
        //pie.title("Expenses on each category")

        pie.labels().position("outside")

        pie.legend().title().enabled(true)
        pie.legend().title()
            .fontColor("#212121")
            .padding(0.0, 0.0, 30.0, 0.0)

        pie.legend()
            .position("bottom")
            .itemsLayout(LegendLayout.HORIZONTAL_EXPANDABLE)
            .align(Align.CENTER)

        binding.chart.setChart(pie)
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_deep_analyze_expense).setOnMenuItemClickListener {

            handleCategoryCompareGraph()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_monthly_graph).setOnMenuItemClickListener {

            handleMonthlyGraphMenu()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_category_graph).setOnMenuItemClickListener {

            handleCategoryGraph()
            true
        }


        binding.toolbar.menu.findItem(R.id.menu_share_expense_graph_sc).setOnMenuItemClickListener {

            handleShareExpenseGraphScreenshotMenu()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_save_expense_graph_sc).setOnMenuItemClickListener {

            handleSaveExpenseGraphScreenshotMenu()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_filter_expense_graph).setOnMenuItemClickListener {

            handleFilterExpenseGraphMenu()
            true
        }

        binding.dateRangeMenuBtn.setOnClickListener { view ->

            showMenuForSelectingCustomTime(view)
        }

        binding.dateRangeCv.setOnClickListener {

            val action =
                GraphFragmentDirections.actionGraphFragmentToShowExpenseBottomSheetFragment(
                    dateRangeMessage = if (selectedCustomDateRangeMenu == CustomDateRange.ALL_TIME) CustomDateRange.ALL_TIME else CustomDateRange.CUSTOM_DATE_RANGE,
                    date1 = d1,
                    date2 = d2,
                    callingFragementTag = ShowExpenseBottomSheetTagsEnum.GRAPH_FRAGMENT,
                    paymentMethodKey = if (expenseFilterDto != null && !expenseFilterDto?.paymentMethods.isNullOrEmpty()) {
                        convertStringListToJSON(expenseFilterDto?.paymentMethods ?: emptyList())
                    } else {
                        null
                    }
                )
            findNavController().navigate(action)
        }
    }

    private fun handleFilterExpenseGraphMenu() {

        //showing Payment Method Selector Dialog

        requireActivity().supportFragmentManager.let { fragmentManager ->

            val bundle = Bundle()
            bundle.putString(
                Constants.EXPENSE_FILTER_KEY,
                if (expenseFilterDto == null) "" else expenseFilterDto.convertToJsonString()
            )

            ShowPaymentMethodSelectorDialogFragment.newInstance(
                bundle
            ).apply {
                show(fragmentManager, TAG)
            }.setOnClickListener(this)
        }

    }

    override fun onFilterApply(selectedPaymentMethods: List<String>?) {

        binding.toolbar.menu.findItem(R.id.menu_filter_expense_graph)
            .apply {

                if (selectedPaymentMethods.isNullOrEmpty()) {
                    this.icon =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_filter_list_24
                        )

                    expenseFilterDto = null

                } else {
                    this.icon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.baseline_filter_list_colored_24
                    )

                    expenseFilterDto = ExpenseFilterDto()
                    expenseFilterDto!!.isPaymentMethodEnabled = true
                    expenseFilterDto!!.paymentMethods = selectedPaymentMethods
                }
            }

        if (selectedCustomDateRangeMenu == CustomDateRange.ALL_TIME) {
            // making this value to false, so it will enter the ALL_TIME case in handleDateRangeSelectionMenu function
            isAllTimeSelected = false
        }

        handleDateRangeSelectionMenu(selectedCustomDateRangeMenu ?: CustomDateRange.ALL_TIME)
    }

    private fun handleSaveExpenseGraphScreenshotMenu() {

        binding.toolbar.hide()

        val bitmap = binding.root.loadBitmap()

        binding.toolbar.show()

        bitmap.saveToStorage(requireContext(), "${getUid()}_expense_graph")

        showToast(requireContext(), getString(R.string.screenshot_saved_to_phone_storage))
    }

    private fun handleShareExpenseGraphScreenshotMenu() {

        binding.toolbar.hide()

        val bitmap = binding.root.loadBitmap()

        binding.toolbar.show()

        lifecycleScope.launch {

            saveBitmapToCacheDirectoryAndShare(requireActivity(), bitmap)
        }
    }

    private fun handleCategoryGraph() {

        expenseViewModel.isAnyExpenseAdded().observe(viewLifecycleOwner) {

            if (it) {
                val action = GraphFragmentDirections.actionGraphFragmentToMonthlyGraphFragment(
                    true
                )
                findNavController().navigate(action)
            } else {

                showToast(requireContext(), getString(R.string.no_expense_added))
            }
        }

    }

    private fun handleMonthlyGraphMenu() {

        expenseViewModel.isAnyExpenseAdded().observe(viewLifecycleOwner) {

            if (it) {
                findNavController().navigate(R.id.action_graphFragment_to_monthlyGraphFragment)
            } else {

                showToast(requireContext(), getString(R.string.no_expense_added))
            }
        }
    }

    private fun handleCategoryCompareGraph() {

        expenseCategoryViewModel.getAllExpenseCategoriesByLimit(2).observe(viewLifecycleOwner) {

            if (it.size >= 2) {

                findNavController().navigate(R.id.action_graphFragment_to_deepAnalyzeExpenseFragment)
            } else {

                showToast(
                    requireContext(),
                    getString(R.string.depp_analyze_error_message),
                    Toast.LENGTH_LONG
                )
            }
        }

    }

    private fun showMenuForSelectingCustomTime(view: View) {

        Functions.showCustomDateRangeOptionMenu(
            requireActivity(),
            view
        ) { selectedMenu ->

            selectedCustomDateRangeMenu = selectedMenu
            saveCustomDateRangeValueInSharedPreference()
            handleDateRangeSelectionMenu(selectedMenu)
        }
    }

    private fun handleDateRangeSelectionMenu(selectedMenu: CustomDateRange) {

        Log.d(TAG, "handleDateRangeSelectionMenu: selectedMenu -> $selectedMenu")

        when (selectedMenu) {

            CustomDateRange.ALL_TIME -> {

                if (!isAllTimeSelected) {

                    isAllTimeSelected = true

                    Log.d(
                        TAG,
                        "handleDateRangeSelectionMenu: paymentMethods: ${expenseFilterDto?.paymentMethods}"
                    )

                    expenseGraphDataViewModel.getTotalExpenseAmountsWithTheirExpenseCategoryNames(
                        expenseFilterDto?.paymentMethods ?: emptyList()
                    )

                    changeSelectionUI()

                    pie.title(getString(R.string.all_time))
                }
            }

            CustomDateRange.LAST_30_DAYS -> {

                isAllTimeSelected = false

                d1 = System.currentTimeMillis() - (30 * ONE_DAY_MILLISECONDS)
                d2 = System.currentTimeMillis()

                getExpenseByDateRange()

                pie.title(getString(R.string.last_30_days))
            }

            CustomDateRange.LAST_7_DAYS -> {

                isAllTimeSelected = false
                d1 = System.currentTimeMillis() - (7 * ONE_DAY_MILLISECONDS)
                d2 = System.currentTimeMillis()

                getExpenseByDateRange()

                pie.title(getString(R.string.last_7_days))
            }

            CustomDateRange.LAST_365_DAYS -> {

                isAllTimeSelected = false

                d1 = System.currentTimeMillis() - (365 * ONE_DAY_MILLISECONDS)
                d2 = System.currentTimeMillis()

                getExpenseByDateRange()

                pie.title(getString(R.string.last_365_days))
            }

            CustomDateRange.CUSTOM_DATE_RANGE -> {

                Functions.showDateRangePickerDialog(
                    d1,
                    d2, {
                        requireActivity().supportFragmentManager
                    },
                    { dates: Pair<Long, Long> ->

                        d1 = dates.first
                        d2 = dates.second

                        isAllTimeSelected = false

                        getExpenseByDateRange()
                    }
                )

                pie.title(getString(R.string.custom_range))
            }

            else -> {

                Log.d(TAG, "date range: selectedMenu -> $selectedMenu")

                isAllTimeSelected = false

                val pair = Functions.getMillisecondsOfStartAndEndUsingConstants(
                    selectedMenu
                )

                d1 = pair.first
                d2 = pair.second

                getExpenseByDateRange()

                when (selectedMenu) {

                    CustomDateRange.THIS_MONTH -> pie.title(getString(R.string.this_month))
                    CustomDateRange.THIS_WEEK -> pie.title(getString(R.string.this_week))
                    CustomDateRange.PREVIOUS_MONTH -> pie.title(getString(R.string.previous_month))
                    CustomDateRange.PREVIOUS_WEEK -> pie.title(getString(R.string.previous_week))
                    else -> {}
                }

            }
        }

    }

    private fun getExpenseByDateRange() {

        if (!isAllTimeSelected) {

            changeSelectionUI()

            expenseGraphDataViewModel.getTotalExpenseAmountsWithTheirExpenseCategoryNamesByDateRange(
                date1 = d1,
                date2 = d2 + ONE_DAY_MILLISECONDS,
                paymentMethodKeys = expenseFilterDto?.paymentMethods ?: emptyList()
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeSelectionUI() {

        if (isAllTimeSelected) {

            binding.dateRangeTv.text = getString(R.string.all_time)
        } else {

            binding.dateRangeTv.text = "${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    d1
                )
            } to ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    d2
                )
            }"

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        saveCustomDateRangeValueInSharedPreference()

        _binding = null
    }

    private fun saveCustomDateRangeValueInSharedPreference() {

        if (selectedCustomDateRangeMenu != CustomDateRange.CUSTOM_DATE_RANGE) {

            requireActivity().saveAnyObjectToSharedPreference(
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME,
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY,
                selectedCustomDateRangeMenu
            )
        }
    }
}
