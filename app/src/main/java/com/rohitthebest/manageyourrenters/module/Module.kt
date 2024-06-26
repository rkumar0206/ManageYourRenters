package com.rohitthebest.manageyourrenters.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rohitthebest.manageyourrenters.api.unsplash.UnsplashAPI
import com.rohitthebest.manageyourrenters.database.databases.*
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.BUDGET_AND_INCOME_DATABASE
import com.rohitthebest.manageyourrenters.others.Constants.EMI_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.MONTHLY_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.PARTIAL_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.PAYMENT_METHOD_DATABASE_NAME
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
annotation class UnsplashImageRetrofit

@Module
@InstallIn(SingletonComponent::class)
object Module {

    //==============================Renter Database========================================

    private val migrationRenter_5_6 = object : Migration(5, 6) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'isSupportingDocAdded' INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'supportingDocument' VARCHAR DEFAULT ''")
        }
    }

    private val migration_6_7 = object : Migration(6, 7) {

        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'renter_payment_table' ADD COLUMN 'isSupportingDocAdded' INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE 'renter_payment_table' ADD COLUMN 'supportingDocument' VARCHAR DEFAULT ''")
        }
    }

    private val migrationRenter_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'status' TEXT NOT NULL DEFAULT ''")
        }
    }

    private val migration_8_9 = object : Migration(8, 9) {

        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'imageUrl' TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'occupation' TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'noOfFamilyMembers' INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideRenterDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RenterAndPaymentDatabase::class.java,
        RENTER_AND_PAYMENT_DATABASE_NAME
    )
        .addMigrations(migrationRenter_5_6, migration_6_7, migrationRenter_7_8, migration_8_9)
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

    private val migrationBorrower_1_2 = object : Migration(1, 2) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'borrower_table' ADD COLUMN 'isSupportingDocAdded' INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE 'borrower_table' ADD COLUMN 'supportingDocument' VARCHAR DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun provideBorrowerDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        BorrowerDatabase::class.java,
        BORROWER_DATABASE_NAME
    )
        .addMigrations(migrationBorrower_1_2)
        .build()

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

    //============================== EMI and EMI payment Database========================================

    @Provides
    @Singleton
    fun provideEMIDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        EmiAndEmiPaymentDatabase::class.java,
        EMI_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideEMIDao(db: EmiAndEmiPaymentDatabase) = db.getEmiDao()

    @Provides
    @Singleton
    fun provideEMIPaymentDao(db: EmiAndEmiPaymentDatabase) = db.getEMIPaymentDao()

    // =========================================================================================

    //============================== Expense Database========================================

    private val expense_database_migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'expense_category_table' ADD COLUMN 'isSelected' INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val expense_database_migration_2_3 = object : Migration(2, 3) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'expense_table' ADD COLUMN 'paymentMethods' VARCHAR2 DEFAULT NULL")
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
        .addMigrations(expense_database_migration_2_3)
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

    // ------------------------------ Monthly Payment Database -----------------------------

    private val monthly_payment_database_migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'monthly_payment_table' ADD COLUMN 'monthlyPaymentDateTimeInfo' VARCHAR2  DEFAULT ''")
        }
    }

    private val monthly_payment_database_migration_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'monthly_payment_table' ADD COLUMN 'expenseCategoryKey' TEXT NOT NULL DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun providesMonthlyPaymentDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        MonthlyPaymentDatabase::class.java,
        MONTHLY_PAYMENT_DATABASE_NAME
    )
        .addMigrations(
            monthly_payment_database_migration_1_2,
            monthly_payment_database_migration_2_3
        )
        .build()

    @Provides
    @Singleton
    fun providesMonthlyPaymentCategoryDao(
        db: MonthlyPaymentDatabase
    ) = db.getMonthlyPaymentCategoryDao()

    @Provides
    @Singleton
    fun providesMonthlyPaymentDao(db: MonthlyPaymentDatabase) = db.getMonthlyPaymentDao()

    // =========================================================================================


    // ----------------------------- Payment method ----------------------------------------

    @Singleton
    @Provides
    fun getPaymentMethodDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        PaymentMethodDatabase::class.java,
        PAYMENT_METHOD_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun getPaymentMethodDao(db: PaymentMethodDatabase) = db.getPaymentMethodDao()

    // ==================================================================================


    // ----------------------------- Income and Budget ----------------------------------------

    @Singleton
    @Provides
    fun getBudgetAndIncomeDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        BudgetAndIncomeDatabase::class.java,
        BUDGET_AND_INCOME_DATABASE
    ).build()

    @Provides
    @Singleton
    fun getBudgetDao(db: BudgetAndIncomeDatabase) = db.getBudgetDao()

    @Provides
    @Singleton
    fun getIncomeDao(db: BudgetAndIncomeDatabase) = db.getIncomeDao()

    // ==================================================================================


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
    // ==================================================================================


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