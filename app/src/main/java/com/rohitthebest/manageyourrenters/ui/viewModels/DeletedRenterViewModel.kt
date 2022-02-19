package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.repositories.DeletedRenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletedRenterViewModel @Inject constructor(
    private val repository: DeletedRenterRepository
) : ViewModel() {

    fun insertDeletedRenter(deletedRenter: DeletedRenter) = viewModelScope.launch {
        repository.insertDeletedRenter(deletedRenter)
    }

    fun insertAllDeletedRenter(deletedRenters: List<DeletedRenter>) = viewModelScope.launch {
        repository.insertAllDeletedRenter(deletedRenters)
    }

    fun updateDeletedRenter(deletedRenter: DeletedRenter) = viewModelScope.launch {
        repository.updateDeletedRenter(deletedRenter)
    }

    fun deleteDeletedRenter(deletedRenter: DeletedRenter) = viewModelScope.launch {
        repository.deleteDeletedRenter(deletedRenter)
    }

    fun deleteAllDeletedRenters() = viewModelScope.launch {
        repository.deleteAllDeletedRenters()
    }

    fun getAllDeletedRenters() = repository.getAllDeletedRenters().asLiveData()

    fun getDeletedRenterByKey(deletedRenterKey: String) =
        repository.getDeletedRenterByKey(deletedRenterKey).asLiveData()

}