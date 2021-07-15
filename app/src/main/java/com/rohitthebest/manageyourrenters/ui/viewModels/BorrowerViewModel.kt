package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.repositories.BorrowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BorrowerViewModel @Inject constructor(
    private val repository: BorrowerRepository,
) : ViewModel() {


    fun insertBorrower(borrower: Borrower) = viewModelScope.launch {

        repository.insertBorrower(borrower)
    }

    fun insertBorrowers(borrowers: List<Borrower>) = viewModelScope.launch {

        repository.insertBorrowers(borrowers)
    }

    fun updateBorrower(borrower: Borrower) = viewModelScope.launch {

        repository.update(borrower)
    }

    fun deleteBorrower(borrower: Borrower) = viewModelScope.launch {

        repository.delete(borrower)
    }

    fun deleteAllBorrower() = viewModelScope.launch {

        repository.deleteAllBorrower()
    }

    fun deleteBorrowerByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        repository.deleteBorrowerByIsSynced(isSynced)
    }

    fun getAllBorrower() = repository.getAllBorrower().asLiveData()

    fun getBorrowerByKey(borrowerKey: String) =
        repository.getBorrowerByKey(borrowerKey).asLiveData()

    fun getBorrowerByIsSynced(isSynced: Boolean) =
        repository.getBorrowerByIsSynced(isSynced).asLiveData()

}