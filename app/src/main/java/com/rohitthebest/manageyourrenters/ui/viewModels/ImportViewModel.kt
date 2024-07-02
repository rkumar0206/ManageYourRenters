package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor() : ViewModel() {

    private val _importLimit = MutableLiveData<Int>().apply {
        // default
        value = 0
    }
    val importLimit: LiveData<Int> = _importLimit

    fun updateImportLimit(value: Int) {

        _importLimit.value = value
    }

}