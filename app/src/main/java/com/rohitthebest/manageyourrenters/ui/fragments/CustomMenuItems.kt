package com.rohitthebest.manageyourrenters.ui.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.CustomMenuBottomsheetLayoutBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show

private const val TAG = "CustomMenuItems"

class CustomMenuItems : BottomSheetDialogFragment(), View.OnClickListener {

    private var _binding: CustomMenuBottomsheetLayoutBinding? = null
    private val binding get() = _binding!!

    private var mListener: OnItemClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.custom_menu_bottomsheet_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = CustomMenuBottomsheetLayoutBinding.bind(view)

        intiListeners()

        getMessage()
    }

    private fun getMessage() {


        if (arguments != null && !arguments?.isEmpty!!) {


            arguments?.let { bundle ->

                val isEditMenuVisible = bundle.getBoolean(Constants.SHOW_EDIT_MENU, true)
                val isDeleteMenuVisible = bundle.getBoolean(Constants.SHOW_DELETE_MENU, true)
                val isDocumentMenuVisible = bundle.getBoolean(Constants.SHOW_DOCUMENTS_MENU, true)
                val isSyncMenuVisible = bundle.getBoolean(Constants.SHOW_SYNC_MENU, false)

                Log.d(TAG, "getMessage: sync_menu : $isSyncMenuVisible")

                if (!isEditMenuVisible) {

                    binding.editEmiMenu.hide()
                } else {

                    binding.editEmiMenu.show()
                }

                if (!isDeleteMenuVisible) {

                    binding.deleteEmiMenu.hide()
                } else {

                    binding.deleteEmiMenu.show()
                }

                if (!isDocumentMenuVisible) {

                    binding.viewOrDownloadSupportingDocumentMenu.hide()
                    binding.replaceSupportingDocumentMenu.hide()
                    binding.deleteSupportingDocumentMenu.hide()
                } else {

                    binding.viewOrDownloadSupportingDocumentMenu.show()
                    binding.replaceSupportingDocumentMenu.show()
                    binding.deleteSupportingDocumentMenu.show()
                }

                if (!isSyncMenuVisible) {

                    binding.syncMenu.hide()
                } else {

                    binding.syncMenu.show()
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
        binding.syncMenu.setOnClickListener(this)
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

            binding.syncMenu.id -> {

                mListener?.onSyncMenuClick()
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
        fun onSyncMenuClick()
    }

    fun setOnClickListener(listener: OnItemClickListener) {

        mListener = listener
    }

    companion object {

        @JvmStatic
        fun newInstance(bundle: Bundle?): CustomMenuItems {

            val fragment = CustomMenuItems()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}