package com.rohitthebest.manageyourrenters.api.services

import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategoryResponse
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

    @GET("/api/{uid}/expenseCategories/key/{key}")
    suspend fun getCategoryByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<ExpenseCategory>


    @PUT("/api/{uid}/expenseCategories/{id}")
    suspend fun updateCategoryById(
        @Path("uid") uid: String,
        @Path("id") id: Long,
        @Body expenseCategory: ExpenseCategory
    ): Response<ExpenseCategory>

    @PUT("/api/{uid}/expenseCategories/key/{key}")
    suspend fun updateCategoryByKey(
        @Path("uid") uid: String,
        @Path("key") key: String,
        @Body expenseCategory: ExpenseCategory
    ): Response<ExpenseCategory>


    @DELETE("/api/{uid}/expenseCategories/{id}")
    suspend fun deleteCategoryById(
        @Path("uid") uid: String,
        @Path("id") id: Long
    ): Response<String?>

    @DELETE("/api/{uid}/expenseCategories/key/{key}")
    suspend fun deleteCategoryByKey(
        @Path("uid") uid: String,
        @Path("key") key: String
    ): Response<String?>

}