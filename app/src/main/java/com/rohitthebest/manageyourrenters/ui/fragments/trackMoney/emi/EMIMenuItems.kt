package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.EmiMenuBottomsheetLayoutBinding

class EMIMenuItems : BottomSheetDialogFragment() {

    private var _binding: EmiMenuBottomsheetLayoutBinding? = null
    private val binding get() = _binding!!

    private var mListener: OnItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.emi_menu_bottomsheet_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = EmiMenuBottomsheetLayoutBinding.bind(view)

        binding.editEmiMenu.setOnClickListener {

            mListener?.onEditMenuClick()
            dismiss()
        }

        binding.deleteEmiMenu.setOnClickListener {

            mListener?.onDeleteMenuClick()
            dismiss()
        }

        binding.supportingDocumentMenu.setOnClickListener {

            mListener?.onSupportingDocumentMenuClick()
            dismiss()
        }
    }

    interface OnItemClickListener {

        fun onEditMenuClick()
        fun onDeleteMenuClick()
        fun onSupportingDocumentMenuClick()
    }

    fun setOnClickListener(listener: OnItemClickListener) {

        mListener = listener
    }

    companion object {

        @JvmStatic
        fun newInstance(bundle: Bundle?): EMIMenuItems {

            val fragment = EMIMenuItems()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}