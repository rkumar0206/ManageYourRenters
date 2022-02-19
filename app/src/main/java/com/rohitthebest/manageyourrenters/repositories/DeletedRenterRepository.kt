package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.DeletedRenterDao
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import javax.inject.Inject

class DeletedRenterRepository @Inject constructor(
    val dao: DeletedRenterDao
) {

    suspend fun insertDeletedRenter(deletedRenter: DeletedRenter) =
        dao.insertDeletedRenter(deletedRenter)

    suspend fun insertAllDeletedRenter(deletedRenters: List<DeletedRenter>) =
        dao.insertAllDeletedRenter(deletedRenters)

    suspend fun updateDeletedRenter(deletedRenter: DeletedRenter) =
        dao.updateDeletedRenter(deletedRenter)

    suspend fun deleteDeletedRenter(deletedRenter: DeletedRenter) =
        dao.deleteDeletedRenter(deletedRenter)

    suspend fun deleteAllDeletedRenters() = dao.deleteAllDeletedRenters()

    fun getAllDeletedRenters() = dao.getAllDeletedRenters()

    fun getDeletedRenterByKey(deletedRenterKey: String) =
        dao.getDeletedRenterByKey(deletedRenterKey)
}