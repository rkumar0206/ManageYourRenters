package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.BorrowerDao
import com.rohitthebest.manageyourrenters.database.model.Borrower
import javax.inject.Inject

class BorrowerRepository @Inject constructor(
    val dao: BorrowerDao
) {

    suspend fun insertBorrower(borrower: Borrower) = dao.insertBorrower(borrower)

    suspend fun insertBorrowers(borrowers: List<Borrower>) = dao.insertBorrowers(borrowers)

    suspend fun update(borrower: Borrower) = dao.update(borrower)

    suspend fun delete(borrower: Borrower) = dao.delete(borrower)

    suspend fun deleteAllBorrower() = dao.deleteAllBorrower()

    fun getAllBorrower() = dao.getAllBorrower()

    fun getBorrowerByKey(borrowerKey: String) = dao.getBorrowerByKey(borrowerKey)

    fun getBorrowerByIsSynced(isSynced: Boolean) = dao.getBorrowerByIsSynced(isSynced)
}