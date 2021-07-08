package com.rohitthebest.manageyourrenters.module

import android.content.Context
import androidx.room.Room
import com.rohitthebest.manageyourrenters.database.databases.BorrowerDatabase
import com.rohitthebest.manageyourrenters.database.databases.PaymentDatabase
import com.rohitthebest.manageyourrenters.database.databases.RenterDatabase
import com.rohitthebest.manageyourrenters.others.Constants.BORROWER_DATABASE_NAME
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

    @Provides
    @Singleton
    fun provideRenterDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        RenterDatabase::class.java,
        RENTER_DATABASE_NAME
    ).build()

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


}