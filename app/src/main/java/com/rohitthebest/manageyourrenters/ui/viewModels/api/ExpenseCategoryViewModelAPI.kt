package com.rohitthebest.manageyourrenters.ui.viewModels.api

import androidx.lifecycle.ViewModel
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseCategoryRepositoryAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExpenseCategoryViewModelAPI @Inject constructor(
    private val expenseCategoryRepositoryAPI: ExpenseCategoryRepositoryAPI
) : ViewModel() {

    //todo : complete this class
}