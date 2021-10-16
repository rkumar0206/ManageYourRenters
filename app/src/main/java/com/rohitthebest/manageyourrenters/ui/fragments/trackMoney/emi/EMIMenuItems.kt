package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.EmiMenuBottomsheetLayoutBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.hide

class EMIMenuItems : BottomSheetDialogFragment(), View.OnClickListener {

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

        intiListeners()

        getMessage()
    }

    private fun getMessage() {


        if (arguments != null && !arguments?.isEmpty!!) {


            arguments?.let { bundle ->

                val isEditMenuVisible = bundle.getBoolean(Constants.SHOW_EDIT_MENU, true)
                val isDeleteMenuVisible = bundle.getBoolean(Constants.SHOW_DELETE_MENU, true)
                val isDocumentMenuVisible = bundle.getBoolean(Constants.SHOW_DOCUMENTS_MENU, true)

                if (!isEditMenuVisible) {

                    binding.editEmiMenu.hide()
                }

                if (!isDeleteMenuVisible) {

                    binding.deleteEmiMenu.hide()
                }

                if (!isDocumentMenuVisible) {

                    binding.viewOrDownloadSupportingDocumentMenu.hide()
                    binding.replaceSupportingDocumentMenu.hide()
                    binding.deleteSupportingDocumentMenu.hide()
                }
            }
        }

    }

    private fun intiListeners() {

        binding.editEmiMenu.setOnClickListener(this)
        binding.deleteEmiMenu.setOnClickListener(this)
        binding.viewOrDownloadSupportingDocumentMenu.setOnClickListener(this)
        binding.replaceSupportingDocumentMenu.setOnClickListener(this)
        binding.deleteSupportingDocumentMenu.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.editEmiMenu.id -> {

                mListener?.onEditMenuClick()
                dismiss()
            }

            binding.deleteEmiMenu.id -> {

                mListener?.onDeleteMenuClick()
                dismiss()
            }

            binding.viewOrDownloadSupportingDocumentMenu.id -> {

                mListener?.onViewSupportingDocumentMenuClick()
                dismiss()
            }

            binding.replaceSupportingDocumentMenu.id -> {

                mListener?.onReplaceSupportingDocumentClick()
                dismiss()
            }

            binding.deleteSupportingDocumentMenu.id -> {

                mListener?.onDeleteSupportingDocumentClick()
                dismiss()
            }

        }

    }


    interface OnItemClickListener {

        fun onEditMenuClick()
        fun onDeleteMenuClick()
        fun onViewSupportingDocumentMenuClick()
        fun onReplaceSupportingDocumentClick()
        fun onDeleteSupportingDocumentClick()
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