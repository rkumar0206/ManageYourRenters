package com.rohitthebest.manageyourrenters.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rohitthebest.manageyourrenters.api.services.ExpenseAPI
import com.rohitthebest.manageyourrenters.api.services.ExpenseCategoryAPI
import com.rohitthebest.manageyourrenters.api.services.MonthlyPaymentAPI
import com.rohitthebest.manageyourrenters.api.services.MonthlyPaymentCategoryAPI
import com.rohitthebest.manageyourrenters.api.unsplash.UnsplashAPI
import com.rohitthebest.manageyourrenters.database.databases.*
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.EMI_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.EMI_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.MANAGE_YOUR_RENTERS_API_BASE_URL
import com.rohitthebest.manageyourrenters.others.Constants.PARTIAL_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.RENTER_AND_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.UNSPLASH_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class ManageYourRentersBackendRetrofit

@Qualifier
annotation class UnsplashImageRetrofit

@Module
@InstallIn(SingletonComponent::class)
object Module {

    //==============================Renter Database========================================

    @Provides
    @Singleton
    fun provideRenterDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RenterAndPaymentDatabase::class.java,
        RENTER_AND_PAYMENT_DATABASE_NAME
    )
        .build()

    @Provides
    @Singleton
    fun provideRenterDao(db: RenterAndPaymentDatabase) = db.getRenterDao()

    @Provides
    @Singleton
    fun provideRenterPaymentDao(db: RenterAndPaymentDatabase) = db.getRenterPaymentDao()

    @Provides
    @Singleton
    fun provideDeletedRenterDao(db: RenterAndPaymentDatabase) = db.getDeletedRenterDao()

//============================== Borrower Database========================================

    @Provides
    @Singleton
    fun provideBorrowerDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        BorrowerDatabase::class.java,
        BORROWER_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideBorrowerDao(db: BorrowerDatabase) = db.getBorrowerDao()

    //============================== Borrower Payment Database========================================

    @Provides
    @Singleton
    fun provideBorrowerPaymentDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        BorrowerPaymentDatabase::class.java,
        BORROWER_PAYMENT_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideBorrowerPaymentDao(db: BorrowerPaymentDatabase) = db.getBorrowerPaymentDao()


    //============================== Partial Payment Database========================================

    @Provides
    @Singleton
    fun providePartialPaymentDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        PartialPaymentDatabase::class.java,
        PARTIAL_PAYMENT_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun providePartialPaymentDao(db: PartialPaymentDatabase) = db.getPartialPaymentDao()

    //============================== EMI Database========================================

    @Provides
    @Singleton
    fun provideEMIDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        EMIDatabase::class.java,
        EMI_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideEMIDao(db: EMIDatabase) = db.getEmiDao()

    //============================== EMI Payment Database========================================

    private val migrationEMIPayment = object : Migration(1, 2) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'emi_payment_table' ADD COLUMN 'message' VARCHAR NOT NULL DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun provideEMIPaymentDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        EMIPaymentDatabase::class.java,
        EMI_PAYMENT_DATABASE_NAME
    )
        .addMigrations(migrationEMIPayment)
        .build()

    @Provides
    @Singleton
    fun provideEMIPaymentDao(db: EMIPaymentDatabase) = db.getEMIPaymentDao()

    // =========================================================================================

    //============================== Expense Database========================================

    private val expense_database_migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'expense_category_table' ADD COLUMN 'isSelected' INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideExpenseDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        ExpenseDatabase::class.java,
        EXPENSE_DATABASE_NAME
    )
        .addMigrations(expense_database_migration_1_2)
        .build()

    @Provides
    @Singleton
    fun provideExpenseCategoryDao(
        db: ExpenseDatabase
    ) = db.getExpenseCategoryDAO()

    @Provides
    @Singleton
    fun provideExpenseDao(
        db: ExpenseDatabase
    ) = db.getExpenseDAO()


    // =========================================================================================


    // ------------------------------ Expense Category API -----------------------------------

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @ManageYourRentersBackendRetrofit
    @Provides
    @Singleton
    fun provideManageYourRentersBackendRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(MANAGE_YOUR_RENTERS_API_BASE_URL)
        .build()

    @Provides
    @Singleton
    fun provideExpenseCategoryAPI(
        @ManageYourRentersBackendRetrofit retrofit: Retrofit
    ): ExpenseCategoryAPI = retrofit.create(ExpenseCategoryAPI::class.java)


    /*   @Provides
       @Singleton
       fun provideExpenseCategoryRepositoryAPI(
           expenseCategoryAPI: ExpenseCategoryAPI
       ) = ExpenseCategoryRepositoryAPI(expenseCategoryAPI)

   */
    // ======================================================================================

    // ------------------------------ Expense API -----------------------------------

    @Provides
    @Singleton
    fun provideExpenseAPI(
        @ManageYourRentersBackendRetrofit retrofit: Retrofit
    ): ExpenseAPI = retrofit.create(ExpenseAPI::class.java)
    // ======================================================================================


    // ------------------------- Monthly Payments API -------------------

    @Provides
    @Singleton
    fun provideMonthlyPaymentCategoryAPI(
        @ManageYourRentersBackendRetrofit retrofit: Retrofit
    ): MonthlyPaymentCategoryAPI = retrofit.create(MonthlyPaymentCategoryAPI::class.java)

    @Provides
    @Singleton
    fun provideMonthlyPaymentAPI(
        @ManageYourRentersBackendRetrofit retrofit: Retrofit
    ): MonthlyPaymentAPI = retrofit.create(MonthlyPaymentAPI::class.java)

    // ==========================================================


    // ----------------------------- Unsplash API ----------------------------------------

    @UnsplashImageRetrofit
    @Singleton
    @Provides
    fun provideUnsplashRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(UNSPLASH_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideUnsplashAPI(
        @UnsplashImageRetrofit retrofit: Retrofit
    ): UnsplashAPI = retrofit.create(UnsplashAPI::class.java)


    // ==================================================================================

}