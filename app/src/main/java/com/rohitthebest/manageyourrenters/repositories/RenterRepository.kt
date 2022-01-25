package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.RenterDao
import com.rohitthebest.manageyourrenters.database.model.Renter
import javax.inject.Inject


class RenterRepository @Inject constructor(
    val dao : RenterDao
) {

    suspend fun insertRenter(renter: Renter) = dao.insertRenter(renter)

    suspend fun insertRenters(renters: List<Renter>) = dao.insertRenters(renters)

    suspend fun updateRenter(renter: Renter) = dao.updateRenter(renter)

    suspend fun deleteRenter(renter: Renter) = dao.deleteRenter(renter)

    suspend fun deleteAllRenter() = dao.deleteAll()

    suspend fun deleteRenterByIsSynced(isSynced: String) = dao.deleteRenterByIsSynced(isSynced)

    fun getAllRentersList() = dao.getAllRentersList()

    fun getRenterCount() = dao.getRentersCount()

    fun getRenterByIsSynced(isSynced: String) = dao.getRenterByIsSynced(isSynced)

    fun getRenterByKey(renterKey: String) = dao.getRenterByKey(renterKey)
}