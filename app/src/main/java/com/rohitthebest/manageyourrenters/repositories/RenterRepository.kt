package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.RenterDao
import com.rohitthebest.manageyourrenters.database.entity.Renter
import javax.inject.Inject


class RenterRepository @Inject constructor(
    val dao : RenterDao
) {

    suspend fun insertRenter(renter : Renter) = dao.insertRenter(renter)

    suspend fun deleteRenter(renter: Renter) = dao.deleteRenter(renter)

    suspend fun deleteAllRenter() = dao.deleteAll()

    fun getAllRentersList() = dao.getAllRentersList()

    fun getRenterCount() = dao.getRentersCount()

    fun getRenterByIsSynced(isSynced: String) = dao.getRenterByIsSynced(isSynced)

    fun getRenterByKey(renterKey: String) = dao.getRenterByKey(renterKey)
}