package com.rohitthebest.manageyourrenters.api.services

import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentsResponse

import retrofit2.Response
import retrofit2.http.*

interface MonthlyPaymentAPI {

    @POST("/api/{uid}/monthlyPayments/category/key/{categoryKey}")
    suspend fun postMonthlyPaymentUsingCategoryKey(
        @Path("uid") uid: String,
        @Path("categoryKey") categoryKey: String
    ): Response<MonthlyPayment>

    @GET("/api/{uid}/monthlyPayments/key/{key}")
    suspend fun getMonthlyPaymentByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<MonthlyPayment>

    @GET("/api/{uid}/monthlyPayments/category/key/{key}")
    suspend fun getMonthlyPaymentsByCategoryKey(
        @Path("uid") uid: String,
        @Path("key") categoryKey: String
    ): Response<MonthlyPaymentsResponse>

    @GET("/api/{uid}/monthlyPayments/uid")
    suspend fun getMonthlyPaymentsByUid(
        @Path("uid") uid: String
    ): Response<MonthlyPaymentsResponse>

    @PUT("/api/{uid}/monthlyPayments/key/{key}/category/key/{categoryKey}")
    suspend fun updateMonthPaymentByKey(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Path("categoryKey") categoryKey: String
    ): Response<MonthlyPayment>

    @DELETE("/api/{uid}/monthlyPayments/key/{key}")
    suspend fun deleteMonthlyPaymentByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<String>

}