package com.rohitthebest.manageyourrenters.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.EditTextBottomSheetLayoutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditTextBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: EditTextBottomSheetLayoutBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.edit_text_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = EditTextBottomSheetLayoutBinding.bind(view)

        // todo : get all the list of the payment methods and map the payment method names to a list
        // todo : when user add any payment method validate it against the payment method name list
        // todo : add the payment method once all the validation is successful

        initListeners()
        textWatchers()
    }

    private fun initListeners() {
        //TODO("Not yet implemented")
    }

    private fun textWatchers() {
        //TODO("Not yet implemented")
    }


    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): EditTextBottomSheetFragment {
            val fragment = EditTextBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}