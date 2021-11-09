package com.rohitthebest.manageyourrenters.api.unsplash

import com.rohitthebest.manageyourrenters.BuildConfig.UNSPLASH_CLIENT_ID
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


interface UnsplashAPI {


    //getting photos related to search query
    @Headers("Accept-Version: v1", "Authorization: Client-ID $UNSPLASH_CLIENT_ID")
    @GET("search/photos")
    suspend fun searchPhoto(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): UnsplashResponse

}