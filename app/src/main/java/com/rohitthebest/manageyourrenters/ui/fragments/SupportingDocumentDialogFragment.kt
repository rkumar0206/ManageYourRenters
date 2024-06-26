package com.rohitthebest.manageyourrenters.ui.fragments

import android.Manifest
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.databinding.AddSupportingDocumentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddSupportingDocumentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.others.Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isPermissionGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "SupportingDocument"

@AndroidEntryPoint
class SupportingDocumentDialogFragment : BottomSheetDialogFragment(),
    RadioGroup.OnCheckedChangeListener {

    private var _binding: FragmentAddSupportingDocumentBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddSupportingDocumentLayoutBinding

    private lateinit var supportingDocumentModel: SupportingDocumentHelperModel

    private var docType: DocumentType = DocumentType.PDF
    private var pdfUri: Uri? = null
    private var imageUri: Uri? = null
    private var fileSize = 0L

    private var mListener: OnBottomSheetDismissListener? = null

    private var isDocumentAdded = false
    private var recentUrlName = ""
    private var imageFileName = ""
    private var pdfFileName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_add_supporting_document, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddSupportingDocumentBinding.bind(view)
        includeBinding = binding.includeLayout

        initListeners()
        textWatchers()

        getMessage()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                supportingDocumentModel = try {

                    val supportingDocumentHelperModelString = bundle.getString(
                        SUPPORTING_DOCUMENT_HELPER_MODEL_KEY
                    )

                    if (supportingDocumentHelperModelString.isValid()) {

                        val sd = supportingDocumentHelperModelString?.convertJsonToObject(
                            SupportingDocumentHelperModel::class.java
                        )!!

                        updateUI(sd)

                        sd
                    } else {

                        SupportingDocumentHelperModel()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                    SupportingDocumentHelperModel()
                }
                Log.d(TAG, "getMessage: $supportingDocumentModel")
            }

        }
    }

    private fun updateUI(supportingDoc: SupportingDocumentHelperModel) {

        docType = supportingDoc.documentType

        if (supportingDoc.documentUri != null) {

            if (docType == DocumentType.PDF) {
                pdfUri = Uri.parse(supportingDoc.documentUri)
            } else if (docType == DocumentType.IMAGE) {
                imageUri = Uri.parse(supportingDoc.documentUri)
            }
        }

        isDocumentAdded = supportingDoc.documentUri != null || supportingDoc.documentUrl.isValid()

        when (docType) {

            DocumentType.PDF -> {
                includeBinding.docTypeRG.check(includeBinding.pdfRB.id)
                pdfFileName = supportingDoc.documentName
                onCheckedChanged(includeBinding.docTypeRG, includeBinding.pdfRB.id)
            }
            DocumentType.IMAGE -> {
                imageFileName = supportingDoc.documentName
                includeBinding.docTypeRG.check(includeBinding.imageRB.id)
            }
            DocumentType.URL -> {
                recentUrlName = supportingDoc.documentName
                includeBinding.urlET.setText(supportingDoc.documentUrl)
                includeBinding.docTypeRG.check(includeBinding.urlRB.id)
            }
        }
    }


    private fun initListeners() {

        binding.addSupportingDocToolbar.setNavigationOnClickListener {

            showToast(requireContext(), getString(R.string.cancelled))
            dismiss()
        }

        binding.addSupportingDocToolbar.menu.findItem(R.id.menu_save_btn)
            .setOnMenuItemClickListener {

                if (isFormValid()) {

                    // send supporting document calling fragment
                    isDocumentAdded = true
                    initSupportingDocumentHelperModel()
                }
                true
            }

        includeBinding.docTypeRG.setOnCheckedChangeListener(this)

        includeBinding.addFileMCV.setOnClickListener {

            if (requireContext().isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                || requireContext().isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            ) {

                if (docType == DocumentType.PDF) {

                    chooseDocumentLauncher.launch("application/pdf")
                } else {

                    chooseDocumentLauncher.launch("image/*")
                }

            } else {

                requestPermission()
            }

        }

        includeBinding.removeFileBtn.setOnClickListener {

            if (docType == DocumentType.PDF) {

                pdfUri = null
            } else {

                imageUri = null
            }

            includeBinding.fileNameET.setText("")

            showRemoveFileBtn(false)
            showFileNameEditText(false)
            showAddFileBtn(true)
        }
    }

    private fun initSupportingDocumentHelperModel() {

        supportingDocumentModel.apply {

            documentType = docType
            documentUri =
                if (docType == DocumentType.PDF) pdfUri.toString() else if (docType == DocumentType.IMAGE) imageUri.toString() else null
            documentName = includeBinding.fileNameET.text.toString().trim()
            documentUrl =
                if (docType == DocumentType.URL) includeBinding.urlET.text.toString().trim() else ""
        }

        dismiss()
    }


    private fun isFormValid(): Boolean {

        if (docType == DocumentType.PDF && pdfUri == null) {

            showToast(requireContext(), "Please add a pdf...")
            return false
        }

        if (docType == DocumentType.IMAGE && imageUri == null) {

            showToast(requireContext(), "Please add an image...")
            return false
        }

        if (docType == DocumentType.PDF || docType == DocumentType.IMAGE) {

            if (fileSize / (1024 * 1024) > 3) {

                showToast(
                    requireContext(),
                    "Supporting document size should be less than or equal to 3MB",
                    Toast.LENGTH_LONG
                )

                return false
            }
        }

        if (!includeBinding.fileNameET.isTextValid()) {

            includeBinding.fileNameET.requestFocus()
            includeBinding.fileNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (includeBinding.fileNameET.text.toString().trim().contains("/")) {

            includeBinding.fileNameET.requestFocus()
            includeBinding.fileNameET.error = "file name should not contain any '/'"
            return false
        }

        if (docType == DocumentType.URL) {

            if (!includeBinding.urlET.isTextValid()) {

                includeBinding.urlET.requestFocus()
                includeBinding.urlET.error = EDIT_TEXT_EMPTY_MESSAGE
                return false
            }
        }

        return includeBinding.fileNameET.error == null
    }

    // adding conditions for requesting permission
    private fun requestPermission() {

        when {

            //check if permission already granted
            requireContext().isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                //permission is granted
                return
            }

            // if the app deems that they should show the request permission rationale
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

                binding.root.showSnackbarWithActionAndDismissListener(
                    "Permission is required for selecting file from your storage.",
                    "Ok",
                    {
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    },
                    {
                        //null
                    }
                )
            }

            // request for permission
            else -> {

                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    //[START OF LAUNCHERS]
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            Log.i(TAG, "Permission granted: ")
        } else {
            Log.i(TAG, "Permission denied: ")
        }
    }

    private val chooseDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->

        uri?.let {

            if (docType == DocumentType.PDF) {

                //pdf uri
                pdfUri = uri
                pdfFileName = ""
            } else {

                // image uri
                imageUri = uri
                imageFileName = ""
            }

            val fileNameAndSize = it.getFileNameAndSize(requireActivity().contentResolver)

            if (fileNameAndSize != null) {

                includeBinding.fileNameET.setText(fileNameAndSize.first)
                fileSize = fileNameAndSize.second
            }

            Log.d(
                TAG,
                "FileNameAndSize: ${fileNameAndSize?.first} " +
                        "and size : ${fileNameAndSize?.second?.div(1024 * 1024)}"
            )

            showAddFileBtn(false)
            showFileNameEditText(true)
            showRemoveFileBtn(true)
        }
    }
    //[END OF LAUNCHERS]

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        hideKeyBoard(requireActivity())

        when (checkedId) {

            includeBinding.pdfRB.id -> {

                handlePdfChoice()
            }

            includeBinding.imageRB.id -> {

                handleImageChoice()
            }

            includeBinding.urlRB.id -> {

                handleUrlChoice()
            }
        }
    }

    private fun handleUrlChoice() {

        includeBinding.fileNameET.hint = "Enter url name here"
        docType = DocumentType.URL

        includeBinding.fileNameET.setText(recentUrlName)

        showUrlEditText(true)
        showFileNameEditText(true)
        showAddFileBtn(false)
        showRemoveFileBtn(false)
    }

    private fun handleImageChoice() {

        includeBinding.fileNameET.hint = "Enter image name"
        docType = DocumentType.IMAGE
        showUrlEditText(false)

        if (imageUri != null) {

            showFileNameEditText(true)
            showRemoveFileBtn(true)
            showAddFileBtn(false)

            val fileNameAndSize =
                imageUri?.getFileNameAndSize(requireActivity().contentResolver)

            includeBinding.fileNameET
                .setText(if (imageFileName.isValid()) imageFileName else fileNameAndSize?.first)

            fileSize = fileNameAndSize?.second!!
        } else {

            showFileNameEditText(false)
            showRemoveFileBtn(false)
            showAddFileBtn(true)
        }
    }

    private fun handlePdfChoice() {

        docType = DocumentType.PDF
        includeBinding.fileNameET.hint = getString(R.string.enter_file_name)
        showUrlEditText(false)

        if (pdfUri != null) {

            showFileNameEditText(true)
            showRemoveFileBtn(true)
            showAddFileBtn(false)

            val fileNameAndSize =
                pdfUri?.getFileNameAndSize(requireActivity().contentResolver)

            includeBinding.fileNameET
                .setText(if (pdfFileName.isValid()) pdfFileName else fileNameAndSize?.first)

            fileSize = fileNameAndSize?.second!!

        } else {

            showFileNameEditText(false)
            showRemoveFileBtn(false)
            showAddFileBtn(true)
        }
    }

    private fun textWatchers() {

        includeBinding.fileNameET.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.fileNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.fileNameET.error = null

                if (docType == DocumentType.URL) {

                    recentUrlName = s.toString()
                }
            }
        }

        includeBinding.urlET.onTextChangedListener { s ->

            if (s?.trim()?.isEmpty()!!) {

                includeBinding.urlET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.urlET.error = null
            }
        }
    }

    private fun showAddFileBtn(isVisible: Boolean) {

        includeBinding.addFileMCV.isVisible = isVisible
    }

    private fun showFileNameEditText(isVisible: Boolean) {

        includeBinding.fileNameET.isVisible = isVisible
    }

    private fun showUrlEditText(isVisible: Boolean) {

        includeBinding.urlET.isVisible = isVisible
        includeBinding.urlET.isEnabled = isVisible
    }

    private fun showRemoveFileBtn(isVisible: Boolean) {

        includeBinding.removeFileBtn.isVisible = isVisible
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        Log.d(TAG, "onDismiss: Supporting document bottom sheet dismissed")

        if (mListener != null) {

            mListener!!.onBottomSheetDismissed(isDocumentAdded, supportingDocumentModel)
        }
    }

    interface OnBottomSheetDismissListener {

        fun onBottomSheetDismissed(
            isDocumentAdded: Boolean,
            supportingDocumentHelperModel: SupportingDocumentHelperModel
        )
    }

    fun setOnBottomSheetDismissListener(listener: OnBottomSheetDismissListener) {

        mListener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): SupportingDocumentDialogFragment {
            val fragment = SupportingDocumentDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}