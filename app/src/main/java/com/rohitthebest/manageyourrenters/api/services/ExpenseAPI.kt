package com.rohitthebest.manageyourrenters.api.services

import com.rohitthebest.manageyourrenters.data.apiModels.Expense
import com.rohitthebest.manageyourrenters.data.apiModels.ExpenseResponse
import retrofit2.Response
import retrofit2.http.*

interface ExpenseAPI {

    @POST("/api/{uid}/expenses/category/{categoryId}")
    suspend fun postExpense(
        @Path("uid") uid: String,
        @Path("categoryId") categoryId: Long,
        @Body expense: Expense
    ): Response<Expense>


    @GET("/api/{uid}/expenses/category/{categoryId}")
    suspend fun getExpensesByCategoryId(
        @Path("uid") uid: String,
        @Path("categoryId") categoryId: Long
    ): Response<List<Expense>>

    @GET("/api/{uid}/expenses/uid")
    suspend fun getExpensesByUID(
        @Path("uid") uid: String,
    ): Response<ExpenseResponse>

    @GET("/api/{uid}/expenses/{id}")
    suspend fun getExpenseById(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<Expense>

    @PUT("/api/{uid}/expenses/id/{id}/category/{categoryId}")
    suspend fun updateExpense(
        @Path("uid") uid: String,
        @Path("id") id: Long,
        @Path("categoryId") categoryId: Long
    ): Response<Expense>

    @DELETE("/api/{uid}/expenses/{id}")
    suspend fun deleteExpense(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<String?>
}