package com.rohitthebest.manageyourrenters.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rohitthebest.manageyourrenters.database.databases.*
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.EMI_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.PARTIAL_PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.PAYMENT_DATABASE_NAME
import com.rohitthebest.manageyourrenters.others.Constants.RENTER_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    //==============================Renter Database========================================

    private val migration = object : Migration(123, 124) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'renter_table' ADD COLUMN 'modified' INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideRenterDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RenterDatabase::class.java,
        RENTER_DATABASE_NAME
    )
        .addMigrations(migration)
        .build()

    @Provides
    @Singleton
    fun provideCategoryDao(db: RenterDatabase) = db.getRenterDao()

//==============================Payment Database========================================

    @Provides
    @Singleton
    fun providePaymentDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        PaymentDatabase::class.java,
        PAYMENT_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun providePaymentDao(db: PaymentDatabase) = db.getPaymentDao()

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

}