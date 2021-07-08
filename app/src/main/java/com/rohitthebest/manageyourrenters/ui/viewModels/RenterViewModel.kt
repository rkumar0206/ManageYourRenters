package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RenterViewModel @Inject constructor(
    val repo: RenterRepository
) : ViewModel() {

    fun insertRenter(renter: Renter) = viewModelScope.launch {

        repo.insertRenter(renter)
    }

    fun insertRenters(renters: List<Renter>) = viewModelScope.launch {

        repo.insertRenters(renters)
    }

    fun deleteRenter(renter: Renter) = viewModelScope.launch {

        repo.deleteRenter(renter)
    }

    fun deleteAllRenter() = viewModelScope.launch {

        repo.deleteAllRenter()
    }

    fun deleteRenterByIsSynced(isSynced: String) = viewModelScope.launch {

        repo.deleteRenterByIsSynced(isSynced)
    }

    fun getAllRentersList() = repo.getAllRentersList()

    fun getRenterCount() = repo.getRenterCount()

    fun getRenterByIsSynced(isSynced: String) = repo.getRenterByIsSynced(isSynced)

    fun getRenterByKey(renterKey: String) = repo.getRenterByKey(renterKey)
}