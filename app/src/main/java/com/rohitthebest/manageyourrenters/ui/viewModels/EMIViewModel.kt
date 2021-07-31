package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.repositories.EMIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EMIViewModel @Inject constructor(
    private val repository: EMIRepository
) : ViewModel() {

    fun insertEMI(emi: EMI) = viewModelScope.launch {
        repository.insertEMI(emi)
    }

    fun insertAllEMI(emis: List<EMI>) = viewModelScope.launch {
        repository.insertAllEMI(emis)
    }

    fun updateEMI(emi: EMI) = viewModelScope.launch {
        repository.updateEMI(emi)
    }

    fun deleteEMI(emi: EMI) = viewModelScope.launch {
        repository.deleteEMI(emi)
    }

    fun deleteAllEMIs() = viewModelScope.launch {
        repository.deleteAllEMIs()
    }

    fun getAllEMIs() = repository.getAllEMIs().asLiveData()

    fun getEMIByKey(emiKey: String) = repository.getEMIByKey(emiKey).asLiveData()

}