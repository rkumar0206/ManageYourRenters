package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

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
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.AddExpenseCategoryLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.apiViewModels.UnsplashViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddEditExpenseCategoryF"

@AndroidEntryPoint
class AddEditExpenseCategoryFragment : Fragment(R.layout.fragment_add_expense_category),
    UnsplashSearchResultsAdapter.OnClickListener {


    private var _binding: FragmentAddExpenseCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: AddExpenseCategoryLayoutBinding

    private val unsplashViewModel by viewModels<UnsplashViewModel>()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var receivedExpenseCategoryKey: String
    private lateinit var receivedExpenseCategory: ExpenseCategory

    private lateinit var unsplashSearchAdapter: UnsplashSearchResultsAdapter

    private var isMessageReceivedForEditing = false

    private var imageUrl = ""

    private var oldCategoryName = ""
    private var oldCategoryDescription = ""
    private var oldCategoryImageUrl = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddExpenseCategoryBinding.bind(view)

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


    private fun setUpRecyclerView() {

        includeBinding.expenseCategoryImageRV.apply {

            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = unsplashSearchAdapter
        }

        unsplashSearchAdapter.setOnClickListener(this)
    }

    override fun onImageClicked(unsplashPhoto: UnsplashPhoto) {

        imageUrl = unsplashPhoto.urls.regular

        setImageToImageViewUsingGlide(
            requireContext(),
            includeBinding.expenseCatIV,
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
                expenseCategoryImageRV.isVisible = loadState.source.refresh is LoadState.NotLoading
            }
        }
    }

    private fun observeUnsplashSearchResult() {

        unsplashViewModel.unsplashSearchResult.observe(viewLifecycleOwner, {

            Log.d(TAG, "observeUnsplashSearchResult: $it")
            unsplashSearchAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        })
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

            oldCategoryName = receivedExpenseCategory.categoryName
            oldCategoryDescription = receivedExpenseCategory.categoryDescription.toString()
            oldCategoryImageUrl = receivedExpenseCategory.imageUrl.toString()

            imageUrl = receivedExpenseCategory.imageUrl.toString()

            if (receivedExpenseCategory.imageUrl.isValid()) {

                setImageToImageViewUsingGlide(
                    requireContext(),
                    includeBinding.expenseCatIV,
                    receivedExpenseCategory.imageUrl,
                    {},
                    {}
                )

                isImageClearBtnVisible(true)
            } else {

                isImageClearBtnVisible(false)
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

            requireActivity().onBackPressed()

        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            Log.i(TAG, "initListeners: isFormValid : ${isFormValid()}")

            if (isFormValid()) {

                initExpenseCategory()
            }

            true
        }


        includeBinding.expenseCatClearImageBtn.setOnClickListener {

            includeBinding.expenseCatIV.setImageResource(R.drawable.gradient_blue)
            imageUrl = ""
            isImageClearBtnVisible(false)
            includeBinding.expenseCategorySearchTextET.setText("")
        }
    }

    private fun initImageSearchEditText() {

        includeBinding.expenseCategorySearchTextET.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                if (includeBinding.expenseCategorySearchTextET.isTextValid()) {

                    hideKeyBoard(requireActivity())

                    if (isInternetAvailable(requireContext())) {

                        unsplashViewModel.searchImage(includeBinding.expenseCategorySearchTextET.text.toString())
                    } else {

                        showNoInternetMessage(requireContext())
                    }
                }
            }
            true
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

    private fun initExpenseCategory() {

        val expenseCategory: ExpenseCategory

        if (!isMessageReceivedForEditing) {

            expenseCategory = ExpenseCategory(

                null,
                includeBinding.expenseCatAddDescriptionET.text.toString(),
                includeBinding.expenseCatCategoryNameET.editText?.text.toString(),
                imageUrl,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                getUid()!!,
                generateKey("_${getUid()}", 60),
                false
            )

            saveToDatabase(expenseCategory)

        } else {

            expenseCategory = receivedExpenseCategory

            // add conditions for editing to happen
            if (
                includeBinding.expenseCatCategoryNameET.editText?.text.toString()
                    .trim() != oldCategoryName
                || includeBinding.expenseCatAddDescriptionET.text.toString()
                    .trim() != oldCategoryDescription
                || imageUrl != oldCategoryImageUrl
            ) {

                if (imageUrl != oldCategoryImageUrl) {

                    expenseCategory.imageUrl = imageUrl
                }

                expenseCategory.categoryName =
                    includeBinding.expenseCatCategoryNameET.editText?.text.toString().trim()
                expenseCategory.categoryDescription =
                    includeBinding.expenseCatAddDescriptionET.text.toString().trim()
                expenseCategory.modified = System.currentTimeMillis()

                saveToDatabase(expenseCategory)
            } else {

                showToast(requireContext(), "No change detected...")
                requireActivity().onBackPressed()
            }
        }
    }

    private fun saveToDatabase(expenseCategory: ExpenseCategory) {

        if (!isMessageReceivedForEditing) {

            expenseCategoryViewModel.insertExpenseCategory(
                requireContext(),
                expenseCategory
            )

            Log.i(TAG, "saveToDatabase: $expenseCategory")
        } else {

            expenseCategoryViewModel.updateExpenseCategory(
                requireContext(),
                expenseCategory
            )
        }

        requireActivity().onBackPressed()
    }

    private fun isImageClearBtnVisible(isVisible: Boolean) {

        includeBinding.expenseCatClearImageBtn.isVisible = isVisible
        includeBinding.expenseCategorySearchTextET.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


}