package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.*
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import javax.inject.Inject

class UpdateIsSyncedValueForAnyTableRepository @Inject constructor(
    private val borrowerDao: BorrowerDao,
    private val borrowerPaymentDao: BorrowerPaymentDao,
    private val renterDao: RenterDao,
    private val renterPaymentDao: RenterPaymentDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val emiDao: EMIDao,
    private val emiPaymentDao: EMIPaymentDao,
    private val expenseDAO: ExpenseDAO,
    private val expenseCategoryDAO: ExpenseCategoryDAO,
    private val monthlyPaymentDao: MonthlyPaymentDao,
    private val monthlyPaymentCategoryDAO: MonthlyPaymentCategoryDao
) {

    suspend fun updateIsSyncValueToFalse(collection: String, key: String) {
        when (collection) {

            FirestoreCollectionsConstants.RENTERS -> {
                renterDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.RENTER_PAYMENTS -> {
                renterPaymentDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.BORROWERS -> {
                borrowerDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.BORROWER_PAYMENTS -> {
                borrowerPaymentDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.EMIs -> {
                emiDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.EMI_PAYMENTS -> {
                emiPaymentDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.EXPENSE_CATEGORIES -> {
                expenseCategoryDAO.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.EXPENSES -> {
                expenseDAO.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES -> {
                monthlyPaymentCategoryDAO.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.MONTHLY_PAYMENTS -> {
                monthlyPaymentDao.updateIsSyncedValueToFalse(key)
            }

            FirestoreCollectionsConstants.PAYMENT_METHODS -> {
                paymentMethodDao.updateIsSyncedValueToFalse(key)
            }
        }
    }
}