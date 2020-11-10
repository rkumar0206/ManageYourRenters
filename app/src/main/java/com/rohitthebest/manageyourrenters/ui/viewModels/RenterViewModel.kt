package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import kotlinx.coroutines.launch

class RenterViewModel @ViewModelInject constructor(
    val repo : RenterRepository
) : ViewModel() {

    fun insertRenter(renter : Renter) = viewModelScope.launch {

        repo.insertRenter(renter)
    }
    fun deleteRenter(renter: Renter) = viewModelScope.launch {

        repo.deleteRenter(renter)
    }

    fun deleteAllRenter() = viewModelScope.launch {

        repo.deleteAllRenter()
    }

    fun getAllRentersList() = repo.getAllRentersList()

    fun getRenterCount() = repo.getRenterCount()

    fun getRenterByIsSynced(isSynced : String) = repo.getRenterByIsSynced(isSynced)

    fun getRenterByKey(renterKey: String) = repo.getRenterByKey(renterKey)
}