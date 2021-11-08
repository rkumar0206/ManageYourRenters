package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.AddExpenseCategoryLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseCategoryBottomsheetBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.checkIfPermissionsGranted
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.getFileNameAndSize
import com.rohitthebest.manageyourrenters.utils.showSnackbarWithActionAndDismissListener
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddEditExpenseCategoryF"

@AndroidEntryPoint
class AddEditExpenseCategoryFragment : BottomSheetDialogFragment() {


    private var _binding: FragmentAddExpenseCategoryBottomsheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddExpenseCategoryLayoutBinding

    private var imageUri: Uri? = null

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
    }

    private fun initListeners() {


        binding.toolbar.setNavigationOnClickListener {

            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            showToast(requireContext(), "Category saved")

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

                //todo : load the image to the image view
                //todo :  hide the add button and show the cancel button
                this.imageUri = imageUri
            }

        }
    }

    private fun isPermissionGranted(): Boolean {

        return requireContext().checkIfPermissionsGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
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