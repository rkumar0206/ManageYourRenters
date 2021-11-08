package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.AddExpenseCategoryLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseCategoryBottomsheetBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.checkIfPermissionsGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "AddEditExpenseCategoryF"

@AndroidEntryPoint
class AddEditExpenseCategoryFragment : BottomSheetDialogFragment() {


    private var _binding: FragmentAddExpenseCategoryBottomsheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddExpenseCategoryLayoutBinding

    private var imageUri: Uri? = null

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var receivedExpenseCategoryKey: String
    private lateinit var receivedExpenseCategory: ExpenseCategory

    private var isMessageReceivedForEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.fragment_add_expense_category_bottomsheet,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddExpenseCategoryBottomsheetBinding.bind(view)

        includeBinding = binding.includeLayout

        initListeners()

        textWatchers()

        getMessage()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                AddEditExpenseCategoryFragmentArgs.fromBundle(bundle)
            }

            receivedExpenseCategoryKey = args?.expenseCategoryKey!!

            isMessageReceivedForEditing = true
            getExpenseCategory()
        }
    }

    private fun getExpenseCategory() {

        expenseCategoryViewModel.getExpenseCategoryByKey(receivedExpenseCategoryKey)
            .observe(viewLifecycleOwner, { expenseCategory ->

                receivedExpenseCategory = expenseCategory

                updateUI()
            })
    }

    private fun updateUI() {

        if (this::receivedExpenseCategory.isInitialized) {

            if (receivedExpenseCategory.imageUrl.isValid()) {

                setImageToImageViewUsingGlide(
                    requireContext(),
                    includeBinding.expenseCatIV,
                    receivedExpenseCategory.imageUrl,
                    {},
                    {}
                )
            }

            includeBinding.expenseCatCategoryNameET.editText?.setText(receivedExpenseCategory.categoryName)

            if (receivedExpenseCategory.categoryDescription.isValid()) {

                includeBinding.expenseCatAddDescriptionET.setText(receivedExpenseCategory.categoryDescription)
            }
        }
    }

    private fun textWatchers() {

        includeBinding.expenseCatCategoryNameET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.expenseCatCategoryNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.expenseCatCategoryNameET.error = null
            }

        }
    }

    private fun initListeners() {


        binding.toolbar.setNavigationOnClickListener {

            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            Log.i(TAG, "initListeners: isFormValid : ${isFormValid()}")

            if (isFormValid()) {

                uploadImage()
            }

            true
        }

        includeBinding.expenseCatAddImage.setOnClickListener {

            if (isPermissionGranted()) {

                chooseImageLauncher.launch(
                    "image/*"
                )
            } else {

                requestPermission()
            }

        }

        includeBinding.expenseCatClearImageBtn.setOnClickListener {

            includeBinding.expenseCatIV.setImageResource(R.drawable.gradient_blue)
            isAddImageBtnVisible(true)
            imageUri = null
        }
    }


    private fun isFormValid(): Boolean {

        Log.i(
            TAG,
            "isFormValid: ${!includeBinding.expenseCatCategoryNameET.editText.isTextValid()}"
        )

        if (!includeBinding.expenseCatCategoryNameET.editText.isTextValid()) {

            includeBinding.expenseCatCategoryNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return includeBinding.expenseCatCategoryNameET.error == null
    }

    @SuppressLint("SetTextI18n")
    private fun uploadImage() {

        if (imageUri != null) {

            lifecycleScope.launch {

                uploadFileUriOnFirebaseStorage(
                    documentUri = imageUri!!,
                    fileReference = FirebaseStorage.getInstance()
                        .getReference("${getUid()}/ExpenseCategory/image")
                        .child(
                            generateKey("", 60)
                        ),
                    uploadTask = {},
                    progressListener = { task ->

                        binding.progressLL.show()
                        binding.expenseCatProgressBar.show()
                        binding.progressTrackerTV.show()

                        val progress = ((100 * task.bytesTransferred) / task.totalByteCount).toInt()
                        binding.expenseCatProgressBar.progress = progress

                        binding.progressTrackerTV.text = "Uploading Image... $progress"
                    },
                    completeListener = { imageUrl ->

                        binding.expenseCatProgressBar.hide()
                        binding.progressTrackerTV.hide()
                        binding.progressLL.hide()

                        initExpenseCategory(imageUrl)
                    },
                    successListener = {},
                    failureListener = {

                        Log.i(
                            TAG,
                            "uploadImage: Exception in uploading expense category image : " + it.message
                        )
                    }
                )
            }
        } else {

            initExpenseCategory("")
        }
    }


    private fun initExpenseCategory(imageUrl: String) {

        val expenseCategory = ExpenseCategory(

            null,
            includeBinding.expenseCatAddDescriptionET.text.toString(),
            includeBinding.expenseCatCategoryNameET.editText?.text.toString(),
            if (imageUrl != "") imageUrl else null,
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            getUid()!!,
            generateKey("_${getUid()}", 60),
            false
        )

        saveToDatabase(expenseCategory)

    }

    private fun saveToDatabase(expenseCategory: ExpenseCategory) {

        expenseCategoryViewModel.insertExpenseCategory(
            requireContext(),
            expenseCategory
        )

        Log.i(TAG, "saveToDatabase: $expenseCategory")

        dismiss()
    }

    private val chooseImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->

        uri?.let { imageUri ->

            val fileNameAndSize = imageUri.getFileNameAndSize(requireActivity().contentResolver)

            // checking file size in MB
            if (fileNameAndSize?.second?.div((1024 * 1024))!! > 0.5) {

                showToast(
                    requireContext(),
                    "File size should be less than or equal to 500KB",
                    Toast.LENGTH_LONG
                )
            } else {

                Glide.with(this)
                    .load(imageUri)
                    .into(includeBinding.expenseCatIV)

                isAddImageBtnVisible(false)

                this.imageUri = imageUri
            }
        }
    }

    private fun isAddImageBtnVisible(isVisible: Boolean) {

        includeBinding.expenseCatAddImage.isVisible = isVisible
        includeBinding.expenseCatClearImageBtn.isVisible = !isVisible
    }

    private fun isPermissionGranted(): Boolean {

        return requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // adding conditions for requesting permission
    private fun requestPermission() {

        when {

            //check if permission already granted
            isPermissionGranted() -> {

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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}