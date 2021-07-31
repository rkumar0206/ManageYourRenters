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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.AddSupportingDocumentLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddSupportingDocumentBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.others.Constants.SUPPORTING_DOCUMENT_BOTTOM_SHEET_DISMISS_LISTENER_KEY
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.checkIfPermissionsGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.getFileNameAndSize
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import com.rohitthebest.manageyourrenters.utils.showSnackbarWithActionAndDismissListener
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddSupportingDocumentBo"

@AndroidEntryPoint
class AddSupportingDocumentBottomSheetFragment : BottomSheetDialogFragment(),
    RadioGroup.OnCheckedChangeListener {

    private var _binding: FragmentAddSupportingDocumentBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private lateinit var includeBinding: AddSupportingDocumentLayoutBinding

    private var receivedCollectionTag = ""
    private var receivedDocumentKey = ""
    private lateinit var receivedEMI: EMI

    private var docType: DocumentType = DocumentType.PDF
    private var pdfUri: Uri? = null
    private var imageUri: Uri? = null
    private var fileSize = 0L

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

            val args = arguments?.let {

                AddSupportingDocumentBottomSheetFragmentArgs.fromBundle(it)
            }

            receivedCollectionTag = args?.tag!!
            receivedDocumentKey = args.key!!

            when (receivedCollectionTag) {

                getString(R.string.emis) -> {

                    getEMI()
                }
            }
        }
    }

    private fun getEMI() {

        emiViewModel.getEMIByKey(receivedDocumentKey).observe(viewLifecycleOwner, { emi ->

            receivedEMI = emi
        })
    }

    private fun initListeners() {

        binding.addSupportingDocToolbar.setNavigationOnClickListener {

            //todo : close
        }

        binding.addSupportingDocToolbar.menu.findItem(R.id.menu_save_btn)
            .setOnMenuItemClickListener {

                if (isFormValid()) {

                    //todo : save
                }
                true
            }

        includeBinding.docTypeRG.setOnCheckedChangeListener(this)

        includeBinding.addFileMCV.setOnClickListener {

            if (requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {

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
            requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE) -> {

                //permission is granted
            }

            // if the app deems that they should show the request permission rationale
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {

                binding.root.showSnackbarWithActionAndDismissListener(
                    "Permission is required for selecting image from your storage.",
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
            } else {

                // image uri
                imageUri = uri
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

    private var recentUrlName = ""

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

        hideKeyBoard(requireActivity())

        when (checkedId) {

            includeBinding.pdfRB.id -> {

                docType = DocumentType.PDF
                includeBinding.fileNameET.hint = "Enter file name"
                showUrlEditText(false)

                if (pdfUri != null) {

                    showFileNameEditText(true)
                    showRemoveFileBtn(true)
                    showAddFileBtn(false)

                    val fileNameAndSize =
                        pdfUri?.getFileNameAndSize(requireActivity().contentResolver)

                    includeBinding.fileNameET
                        .setText(fileNameAndSize?.first)

                    fileSize = fileNameAndSize?.second!!

                } else {

                    showFileNameEditText(false)
                    showRemoveFileBtn(false)
                    showAddFileBtn(true)
                }

            }

            includeBinding.imageRB.id -> {

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
                        .setText(fileNameAndSize?.first)

                    fileSize = fileNameAndSize?.second!!
                } else {

                    showFileNameEditText(false)
                    showRemoveFileBtn(false)
                    showAddFileBtn(true)
                }
            }

            includeBinding.urlRB.id -> {

                includeBinding.fileNameET.hint = "Enter url name here"
                docType = DocumentType.URL

                includeBinding.fileNameET.setText(recentUrlName)

                showUrlEditText(true)
                showFileNameEditText(true)
                showAddFileBtn(false)
                showRemoveFileBtn(false)
            }
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

        Log.d(TAG, "onDismiss: Supprting document bottom sheet dismissed")
        notifyTheFragment()
    }

    private fun notifyTheFragment() {

        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            SUPPORTING_DOCUMENT_BOTTOM_SHEET_DISMISS_LISTENER_KEY,
            true
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}