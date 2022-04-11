package com.rohitthebest.manageyourrenters.api.services

import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategoryResponse
import retrofit2.Response
import retrofit2.http.*

interface MonthlyPaymentCategoryAPI {

    @POST("/api/{uid}/monthlyPaymentCategories")
    suspend fun postMonthLyPaymentCategory(
        @Path("uid") uid: String,
        @Body monthlyPaymentCategory: MonthlyPaymentCategory
    ): Response<MonthlyPaymentCategory>

    @GET("/api/{uid}/monthlyPaymentCategories/uid")
    suspend fun getMonthlyPaymentCategories(
        @Path("uid") uid: String
    ): Response<MonthlyPaymentCategoryResponse>

    @GET("/api/{uid}/monthlyPaymentCategories/key/{key}")
    suspend fun getMonthlyPaymentsByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<MonthlyPaymentCategory>

    @PUT("/api/{uid}/monthlyPaymentCategories/key/{key}")
    suspend fun updateCategoryByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<MonthlyPaymentCategory>

    @DELETE("/api/{uid}/monthlyPaymentCategories/key/{key}")
    suspend fun deleteCategoryByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<String>

}