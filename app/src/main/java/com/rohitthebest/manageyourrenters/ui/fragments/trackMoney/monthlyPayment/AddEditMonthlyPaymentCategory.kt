package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.unsplashAdapters.UnsplashSearchResultsAdapter
import com.rohitthebest.manageyourrenters.data.UnsplashPhoto
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.AddMonthlyPaymentCategoryLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEditMonthlyPaymentCategoryBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.apiViewModels.UnsplashViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddEditMonthlyPaymentCategory"

@AndroidEntryPoint
class AddEditMonthlyPaymentCategory :
    Fragment(R.layout.fragment_add_edit_monthly_payment_category),
    UnsplashSearchResultsAdapter.OnClickListener {

    private var _binding: FragmentAddEditMonthlyPaymentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddMonthlyPaymentCategoryLayoutBinding

    private val unsplashViewModel by viewModels<UnsplashViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()
    private lateinit var unsplashSearchAdapter: UnsplashSearchResultsAdapter

    private var imageUrl = ""

    private var receivedMonthlyPaymentKey = ""
    private lateinit var receivedMonthlyPaymentCategory: MonthlyPaymentCategory
    private var isMessageReceivedForEditing = false
    private var oldCategoryName = ""
    private var oldCategoryDescription = ""
    private var oldCategoryImageUrl = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditMonthlyPaymentCategoryBinding.bind(view)

        includeBinding = binding.includeLayout

        unsplashSearchAdapter = UnsplashSearchResultsAdapter()

        initListeners()
        observeUnsplashSearchResult()
        textWatchers()
        getMessage()
        setUpRecyclerView()
        setUpLoadStateListener()
        initImageSearchEditText()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                AddEditMonthlyPaymentCategoryArgs.fromBundle(bundle)
            }

            receivedMonthlyPaymentKey = args?.monthlyPaymentCategoryKey!!

            isMessageReceivedForEditing = true
            getMonthlyPaymentCategory()
        }
    }

    private fun getMonthlyPaymentCategory() {

        monthlyPaymentCategoryViewModel.getMonthlyPaymentCategoryUsingKey(receivedMonthlyPaymentKey)
            .observe(viewLifecycleOwner) { category ->

                receivedMonthlyPaymentCategory = category

                updateUI()
            }
    }

    private fun updateUI() {

        if (this::receivedMonthlyPaymentCategory.isInitialized) {

            oldCategoryName = receivedMonthlyPaymentCategory.categoryName
            oldCategoryDescription = receivedMonthlyPaymentCategory.categoryDescription
            oldCategoryImageUrl = receivedMonthlyPaymentCategory.imageUrl

            imageUrl = receivedMonthlyPaymentCategory.imageUrl

            if (receivedMonthlyPaymentCategory.imageUrl.isValid()) {

                Functions.setImageToImageViewUsingGlide(
                    requireContext(),
                    includeBinding.monthlyPaymentCatIV,
                    receivedMonthlyPaymentCategory.imageUrl,
                    {},
                    {}
                )

                isImageClearBtnVisible(true)
            } else {

                isImageClearBtnVisible(false)
            }

            includeBinding.monthlyPaymentCatCategoryNameET.editText?.setText(
                receivedMonthlyPaymentCategory.categoryName
            )

            if (receivedMonthlyPaymentCategory.categoryDescription.isValid()) {

                includeBinding.monthlyPaymentCatAddDescriptionET.setText(
                    receivedMonthlyPaymentCategory.categoryDescription
                )
            }
        }
    }


    private fun setUpRecyclerView() {

        includeBinding.monthlyPaymentCategoryImageRV.apply {

            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = unsplashSearchAdapter
        }

        unsplashSearchAdapter.setOnClickListener(this)
    }

    override fun onImageClicked(unsplashPhoto: UnsplashPhoto) {

        imageUrl = unsplashPhoto.urls.regular

        Functions.setImageToImageViewUsingGlide(
            requireContext(),
            includeBinding.monthlyPaymentCatIV,
            imageUrl,
            {},
            {}
        )

        isImageClearBtnVisible(true)
    }

    private fun setUpLoadStateListener() {

        unsplashSearchAdapter.addLoadStateListener { loadState ->

            includeBinding.apply {

                binding.progressLL.isVisible = loadState.source.refresh is LoadState.Loading
                monthlyPaymentCategoryImageRV.isVisible =
                    loadState.source.refresh is LoadState.NotLoading
            }
        }
    }

    private fun observeUnsplashSearchResult() {

        unsplashViewModel.unsplashSearchResult.observe(viewLifecycleOwner) {

            Log.d(TAG, "observeUnsplashSearchResult: $it")
            unsplashSearchAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun initListeners() {


        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()

        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            Log.i(TAG, "initListeners: isFormValid : ${isFormValid()}")

            if (isFormValid()) {

                initMonthlyPaymentCategory()
            }

            true
        }

        includeBinding.monthlyPaymentCatClearImageBtn.setOnClickListener {

            includeBinding.monthlyPaymentCatIV.setImageResource(R.drawable.gradient_blue)
            imageUrl = ""
            isImageClearBtnVisible(false)
            includeBinding.monthlyPaymentCategorySearchTextET.setText("")
        }
    }

    private fun isFormValid(): Boolean {

        if (!includeBinding.monthlyPaymentCatCategoryNameET.editText.isTextValid()) {

            includeBinding.monthlyPaymentCatCategoryNameET.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return includeBinding.monthlyPaymentCatCategoryNameET.error == null
    }

    private fun initMonthlyPaymentCategory() {

        val monthlyPaymentCategory: MonthlyPaymentCategory

        if (!isMessageReceivedForEditing) {

            monthlyPaymentCategory = MonthlyPaymentCategory(
                Functions.generateKey("_${Functions.getUid()}", 60),
                includeBinding.monthlyPaymentCatAddDescriptionET.text.toString(),
                includeBinding.monthlyPaymentCatCategoryNameET.editText?.text.toString(),
                System.currentTimeMillis(),
                0,
                imageUrl,
                System.currentTimeMillis(),
                Functions.getUid()!!,
                true
            )
            saveToDatabase(monthlyPaymentCategory)

        } else {

            monthlyPaymentCategory = receivedMonthlyPaymentCategory

            // add conditions for editing to happen
            if (
                includeBinding.monthlyPaymentCatCategoryNameET.editText?.text.toString()
                    .trim() != oldCategoryName
                || includeBinding.monthlyPaymentCatAddDescriptionET.text.toString()
                    .trim() != oldCategoryDescription
                || imageUrl != oldCategoryImageUrl
            ) {

                if (imageUrl != oldCategoryImageUrl) {

                    monthlyPaymentCategory.imageUrl = imageUrl
                }

                monthlyPaymentCategory.categoryName =
                    includeBinding.monthlyPaymentCatCategoryNameET.editText?.text.toString().trim()
                monthlyPaymentCategory.categoryDescription =
                    includeBinding.monthlyPaymentCatAddDescriptionET.text.toString().trim()
                monthlyPaymentCategory.modified = System.currentTimeMillis()
                monthlyPaymentCategory.isSynced = true
                saveToDatabase(monthlyPaymentCategory)
            } else {

                Functions.showToast(requireContext(), "No change detected...")
                requireActivity().onBackPressed()
            }
        }
    }

    private fun saveToDatabase(monthlyPaymentCategory: MonthlyPaymentCategory) {

        if (!isMessageReceivedForEditing) {

            monthlyPaymentCategoryViewModel.insertMonthlyPaymentCategory(
                monthlyPaymentCategory
            )
        } else {

            monthlyPaymentCategoryViewModel.updateMonthlyPaymentCategory(
                monthlyPaymentCategory
            )
        }
        requireActivity().onBackPressed()
    }

    private fun initImageSearchEditText() {

        includeBinding.monthlyPaymentCategorySearchTextET.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                if (includeBinding.monthlyPaymentCategorySearchTextET.isTextValid()) {

                    Functions.hideKeyBoard(requireActivity())

                    if (Functions.isInternetAvailable(requireContext())) {

                        unsplashViewModel.searchImage(includeBinding.monthlyPaymentCategorySearchTextET.text.toString())
                    } else {

                        Functions.showNoInternetMessage(requireContext())
                    }
                }
            }
            true
        }
    }

    private fun textWatchers() {

        includeBinding.monthlyPaymentCatCategoryNameET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.monthlyPaymentCatCategoryNameET.error =
                    Constants.EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.monthlyPaymentCatCategoryNameET.error = null
            }

        }
    }

    private fun isImageClearBtnVisible(isVisible: Boolean) {

        includeBinding.monthlyPaymentCatClearImageBtn.isVisible = isVisible
        includeBinding.monthlyPaymentCategorySearchTextET.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Functions.hideKeyBoard(requireActivity())

        _binding = null
    }
}


