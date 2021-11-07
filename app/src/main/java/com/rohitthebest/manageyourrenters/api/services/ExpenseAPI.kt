package com.rohitthebest.manageyourrenters.api.services

import com.rohitthebest.manageyourrenters.data.apiModels.Expense
import com.rohitthebest.manageyourrenters.data.apiModels.ExpenseResponse
import retrofit2.Response
import retrofit2.http.*

interface ExpenseAPI {

    @POST("/api/{uid}/expenses/category/{categoryId}")
    suspend fun postExpenseByCategoryId(
        @Path("uid") uid: String,
        @Path("categoryId") categoryId: Long,
        @Body expense: Expense
    ): Response<Expense>

    @POST("/api/{uid}/expenses/category/key/{categoryKey}")
    suspend fun postExpenseByCategoryKey(
        @Path("uid") uid: String,
        @Path("categoryKey") categoryKey: String,
        @Body expense: Expense
    ): Response<Expense>


    @GET("/api/{uid}/expenses/category/{categoryId}")
    suspend fun getExpensesByCategoryId(
        @Path("uid") uid: String,
        @Path("categoryId") categoryId: Long
    ): Response<List<Expense>>

    @GET("/api/{uid}/expenses/category/key/{key}")
    suspend fun getExpensesByCategoryKey(
        @Path("uid") uid: String,
        @Path("key") categoryKey: String
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

    @GET("/api/{uid}/expenses/key/{key}")
    suspend fun getExpenseByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<Expense>


    @PUT("/api/{uid}/expenses/id/{id}/category/{categoryId}")
    suspend fun updateExpenseById(
        @Path("uid") uid: String,
        @Path("id") id: Long,
        @Path("categoryId") categoryId: Long
    ): Response<Expense>

    @PUT("/api/{uid}/expenses/key/{key}/category/{categoryKey}")
    suspend fun updateExpenseByKey(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Path("categoryKey") categoryKey: String
    ): Response<Expense>


    @DELETE("/api/{uid}/expenses/{id}")
    suspend fun deleteExpenseById(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<String?>

    @DELETE("/api/{uid}/expenses/key/{key}")
    suspend fun deleteExpenseByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<String?>
}