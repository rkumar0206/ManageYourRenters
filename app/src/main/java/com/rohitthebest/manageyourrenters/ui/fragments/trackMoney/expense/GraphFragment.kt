package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.charts.Cartesian
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentGraphBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "GraphFragment"

@AndroidEntryPoint
class GraphFragment : Fragment(R.layout.fragment_graph) {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private lateinit var pie: Pie
    private lateinit var cartesian: Cartesian

    private var isAllTimeSelected = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphBinding.bind(view)

        //setUpCartesianChart()
        setUpPieChart()

        getExpenseCategoryExpensesByAllTime()
        initListeners()
    }


    private fun setUpPieChart() {

        pie = AnyChart.pie()

        binding.chart.setProgressBar(binding.progressBar)

        pie.title().enabled(false)
        //pie.title("Expenses on each category")

        pie.labels().position("outside")

        pie.legend().title().enabled(false)
        pie.legend().title()
            .text("Expense categories")
            .padding(0.0, 0.0, 10.0, 0.0)

        pie.legend()
            .position("bottom")
            .itemsLayout(LegendLayout.HORIZONTAL_EXPANDABLE)
            .align(Align.CENTER)

/*
        pie.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {

                            Toast.makeText(
                                requireContext(),
                                event.data["x"].toString() + ":" + event.data["value"],
                                Toast.LENGTH_SHORT
                            ).show()

            }
        })
*/


    }

    private fun setUpCartesianChart() {

        cartesian = AnyChart.column()
        binding.chart.setProgressBar(binding.progressBar)

        cartesian.title("Expenses on each category")

        cartesian.labels().position("outside")

        cartesian.legend().title().enabled(true)
        cartesian.legend().title()
            .text("Expense categories")
            .padding(0.0, 0.0, 10.0, 0.0)

        cartesian.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        cartesian.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {
/*
                            Toast.makeText(
                                requireContext(),
                                event.data["x"].toString() + ":" + event.data["value"],
                                Toast.LENGTH_SHORT
                            ).show()
*/
            }
        })
    }

    private var d1 = System.currentTimeMillis() - 200000000L
    private var d2 = System.currentTimeMillis()

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.allTimeBtn.setOnClickListener {

            if (!isAllTimeSelected) {

                isAllTimeSelected = true

                getExpenseCategoryExpensesByAllTime()

                changeSelectionUI()
            }

        }

        binding.selectRangeBtn.setOnClickListener {

            Functions.showDateRangePickerDialog(
                d1,
                d2, {
                    requireActivity().supportFragmentManager
                },
                { dates: Pair<Long, Long> ->

                    d1 = dates.first
                    d2 = dates.second

                    isAllTimeSelected = false
                    changeSelectionUI()
                    getExpenseCategoryExpensesByDateRange(dates.first, dates.second)
                }
            )
        }
    }

    private fun getExpenseCategoryExpensesByDateRange(date1: Long?, date2: Long?) {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner, { expenseCategories ->

                if (expenseCategories.isNotEmpty()) {

                    val data = ArrayList<DataEntry>()

                    expenseCategories.forEach { expenseCategory ->

                        lifecycleScope.launch {

                            try {
                                expenseViewModel.getExpenseAmountSumByExpenseCategoryByDateRange(
                                    expenseCategory.key, date1!!, date2!! + 86400000L
                                ).collect { amount ->

                                    Log.d(
                                        TAG,
                                        "getExpenseCategoryExpensesByDateRange: category : ${expenseCategory.categoryName} -> amount : $amount"
                                    )

                                    data.add(
                                        ValueDataEntry(
                                            expenseCategory.categoryName,
                                            amount
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    lifecycleScope.launch {

                        delay(500)

                        Log.d(TAG, "getAllExpenseCategory: $data")

                        if (data.isNotEmpty()) {

                            binding.chart.show()
                            pie.data(data)
                            binding.chart.setChart(pie)
                        } else {

                            binding.chart.hide()
                            showToast(requireContext(), "No data found")
                        }
                    }

                }
            })

    }

    private fun getExpenseCategoryExpensesByAllTime() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner, { expenseCategories ->

                if (expenseCategories.isNotEmpty()) {

                    val data = ArrayList<DataEntry>()

                    expenseCategories.forEach { expenseCategory ->

                        lifecycleScope.launch {

                            try {
                                expenseViewModel.getExpenseAmountSumByExpenseCategoryKey(
                                    expenseCategory.key
                                ).collect { amount ->

                                    Log.d(
                                        TAG,
                                        "getExpenseCategoryExpensesByAllTime: category : ${expenseCategory.categoryName} -> amount : $amount"
                                    )

                                    data.add(
                                        ValueDataEntry(
                                            expenseCategory.categoryName,
                                            amount
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    lifecycleScope.launch {

                        delay(500)

                        Log.d(TAG, "getAllExpenseCategory: $data")

                        if (data.isNotEmpty()) {

                            binding.chart.show()
                            pie.data(data)
                            binding.chart.setChart(pie)
                        } else {

                            binding.chart.hide()
                        }

                    }

                }
            })
    }

    private fun changeSelectionUI() {

        if (isAllTimeSelected) {

            binding.allTimeBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.purple_500
                )
            )
            binding.allTimeBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_white
                )
            )

            binding.selectRangeBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_white
                )
            )
            binding.selectRangeBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primaryTextColor
                )
            )


        } else {

            binding.allTimeBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_white
                )
            )
            binding.allTimeBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primaryTextColor
                )
            )

            binding.selectRangeBtn.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.purple_500
                )
            )
            binding.selectRangeBtn.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_white
                )
            )

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
