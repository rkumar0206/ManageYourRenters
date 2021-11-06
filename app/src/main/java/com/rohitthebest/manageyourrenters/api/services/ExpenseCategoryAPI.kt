package com.rohitthebest.manageyourrenters.api.services

import com.rohitthebest.manageyourrenters.data.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.data.apiModels.ExpenseCategoryResponse
import retrofit2.Response
import retrofit2.http.*

interface ExpenseCategoryAPI {

    @POST("/api/{uid}/expenseCategories")
    suspend fun postExpenseCategory(
        @Path("uid") uid: String,
        @Body expenseCategory: ExpenseCategory
    ): Response<ExpenseCategory>


    @GET("/api/{uid}/expenseCategories/uid")
    suspend fun getExpenseCategories(
        @Path("uid") uid: String
    ): Response<ExpenseCategoryResponse>

    @GET("/api/{uid}/expenseCategories/{id}")
    suspend fun getCategoryById(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<ExpenseCategory>

    @PUT("/api/{uid}/expenseCategories/{id}")
    suspend fun updateCategoryById(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<ExpenseCategory>

    @DELETE("/api/{uid}/expenseCategories/{id}")
    suspend fun deleteCategoryById(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<ExpenseCategory>

}