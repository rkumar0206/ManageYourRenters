package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphBinding.bind(view)

        setUpPieChart()
        //setUpCartesianChart()

        getAllExpenseCategory()
        initListeners()
    }


    private fun setUpPieChart() {

        pie = AnyChart.pie()
        binding.chart.setProgressBar(binding.progressBar)

        pie.title("Expenses on each category")

        pie.labels().position("outside")

        pie.legend().title().enabled(true)
        pie.legend().title()
            .text("Expense categories")
            .padding(0.0, 0.0, 10.0, 0.0)

        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        pie.setOnClickListener(object :
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

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun getAllExpenseCategory() {

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
                                        "getAllExpenseCategory: category : ${expenseCategory.categoryName} -> amount : $amount"
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
                            } finally {

                                data.add(
                                    ValueDataEntry(
                                        expenseCategory.categoryName,
                                        0.0
                                    )
                                )
                            }
                        }
                    }

                    lifecycleScope.launch {

                        delay(500)

                        Log.d(TAG, "getAllExpenseCategory: $data")

                        pie.data(data)
                        //cartesian.data(data)

                        binding.chart.setChart(pie)
                    }

                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
