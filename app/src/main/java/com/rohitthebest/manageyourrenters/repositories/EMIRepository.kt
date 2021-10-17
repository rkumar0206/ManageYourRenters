package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.EMIDao
import com.rohitthebest.manageyourrenters.database.model.EMI
import javax.inject.Inject

class EMIRepository @Inject constructor(
    val dao: EMIDao
) {

    suspend fun insertEMI(emi: EMI) = dao.insertEMI(emi)

    suspend fun insertAllEMI(emis: List<EMI>) =
        dao.insertAllEMI(emis)

    suspend fun updateEMI(emi: EMI) =
        dao.updateEMI(emi)

    suspend fun deleteEMI(emi: EMI) =
        dao.deleteEMI(emi)

    suspend fun deleteAllEMIs() = dao.deleteAllEMIs()

    suspend fun deleteEMIsByIsSynced(isSynced: Boolean) = dao.deleteEMIsByIsSynced(isSynced)

    fun getAllEMIs() = dao.getAllEMIs()

    fun getEMIByKey(emiKey: String) = dao.getEMIByKey(emiKey)
}