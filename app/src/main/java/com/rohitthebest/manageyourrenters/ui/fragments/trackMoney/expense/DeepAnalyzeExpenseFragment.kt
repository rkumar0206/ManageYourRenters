package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentDeepAnalyzeExpenseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeepAnalyzeExpenseFragment : Fragment(R.layout.fragment_deep_analyze_expense) {

    private var _binding: FragmentDeepAnalyzeExpenseBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeepAnalyzeExpenseBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
