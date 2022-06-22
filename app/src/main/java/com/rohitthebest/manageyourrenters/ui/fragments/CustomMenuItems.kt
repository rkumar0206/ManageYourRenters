package com.rohitthebest.manageyourrenters.ui.fragments


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.CustomMenuBottomsheetLayoutBinding
import com.rohitthebest.manageyourrenters.others.Constants

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
                val isCopyMenuVisible = bundle.getBoolean(Constants.SHOW_COPY_MENU, false)
                val isMoveMenuVisible = bundle.getBoolean(Constants.SHOW_MOVE_MENU, false)

                Log.d(TAG, "getMessage: sync_menu : $isSyncMenuVisible")

                binding.editMenu.isVisible = isEditMenuVisible
                binding.deleteMenu.isVisible = isDeleteMenuVisible
                binding.viewOrDownloadSupportingDocumentMenu.isVisible = isDocumentMenuVisible
                binding.replaceSupportingDocumentMenu.isVisible = isDocumentMenuVisible
                binding.deleteSupportingDocumentMenu.isVisible = isDocumentMenuVisible
                binding.syncMenu.isVisible = isSyncMenuVisible
                binding.copyMenu.isVisible = isCopyMenuVisible
                binding.moveMenu.isVisible = isMoveMenuVisible
            }
        }

    }

    private fun intiListeners() {

        binding.editMenu.setOnClickListener(this)
        binding.deleteMenu.setOnClickListener(this)
        binding.viewOrDownloadSupportingDocumentMenu.setOnClickListener(this)
        binding.replaceSupportingDocumentMenu.setOnClickListener(this)
        binding.deleteSupportingDocumentMenu.setOnClickListener(this)
        binding.syncMenu.setOnClickListener(this)
        binding.copyMenu.setOnClickListener(this)
        binding.moveMenu.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.editMenu.id -> {
                mListener?.onEditMenuClick()
            }

            binding.deleteMenu.id -> {
                mListener?.onDeleteMenuClick()
            }

            binding.viewOrDownloadSupportingDocumentMenu.id -> {
                mListener?.onViewSupportingDocumentMenuClick()
            }

            binding.replaceSupportingDocumentMenu.id -> {
                mListener?.onReplaceSupportingDocumentClick()
            }

            binding.deleteSupportingDocumentMenu.id -> {
                mListener?.onDeleteSupportingDocumentClick()
            }

            binding.syncMenu.id -> {
                mListener?.onSyncMenuClick()
            }

            binding.copyMenu.id -> mListener?.onCopyMenuClick()
            binding.moveMenu.id -> mListener?.onMoveMenuClick()

        }

        dismiss()

    }


    interface OnItemClickListener {

        fun onEditMenuClick()
        fun onCopyMenuClick()
        fun onMoveMenuClick()
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