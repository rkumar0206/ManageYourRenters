package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.SelectMonthAndYearAdapter
import com.rohitthebest.manageyourrenters.databinding.FragmentChooseMonthAndYearBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.convertJSONToStringList

private const val TAG = "ChooseMonthAndYearBotto"

class ChooseMonthAndYearBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.fragment_choose_month_and_year),
    SelectMonthAndYearAdapter.OnClickListener {

    private var _binding: FragmentChooseMonthAndYearBinding? = null
    private val binding get() = _binding!!

    private var mListener: OnBottomSheetDismissListener? = null
    private lateinit var selectMonthAndYearAdapter: SelectMonthAndYearAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChooseMonthAndYearBinding.bind(view)

        selectMonthAndYearAdapter = SelectMonthAndYearAdapter()
        setUpRecyclerView()

        getMessage()

        binding.toolbar.setNavigationOnClickListener {

            if (mListener != null) {

                mListener!!.onMonthAndYearSelectedForCopyingBudget(false, "")
            }
            dismiss()
        }
    }

    private fun setUpRecyclerView() {

        binding.monthAndYearRV.apply {

            setHasFixedSize(true)
            adapter = selectMonthAndYearAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        selectMonthAndYearAdapter.setOnClickListener(this)
    }

    override fun onMonthAndYearClicked(monthAndYear: String) {

        if (mListener != null) {
            mListener!!.onMonthAndYearSelectedForCopyingBudget(
                true,
                monthAndYear
            )

            dismiss()
        }
    }


    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                try {

                    val monthYearList =
                        convertJSONToStringList(bundle.getString(Constants.COPY_BUDGET_MONTH_AND_YEAR_KEY))

                    Log.d(TAG, "getMessage: monthYearString: $monthYearList")

                    selectMonthAndYearAdapter.submitList(monthYearList)

                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    requireContext().showToast(getString(R.string.something_went_wrong))
                    dismiss()
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ChooseMonthAndYearBottomSheetFragment {
            val fragment = ChooseMonthAndYearBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    interface OnBottomSheetDismissListener {

        fun onMonthAndYearSelectedForCopyingBudget(
            isMonthAndYearSelected: Boolean,
            selectedMonthYearString: String
        )
    }

    fun setOnBottomSheetDismissListener(listener: OnBottomSheetDismissListener) {

        mListener = listener
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
