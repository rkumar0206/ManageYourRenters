package com.rohitthebest.manageyourrenters.api.unsplash

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rohitthebest.manageyourrenters.data.UnsplashPhoto
import com.rohitthebest.manageyourrenters.others.Constants.NETWORK_PAGE_SIZE_UNSPLASH
import retrofit2.HttpException
import java.io.IOException

class UnsplashPagingSource(
    private val unsplashAPI: UnsplashAPI,
    private val searchQuery: String
) : PagingSource<Int, UnsplashPhoto>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UnsplashPhoto> {

        val position = params.key ?: 1

        return try {

            val response = unsplashAPI.searchPhoto(searchQuery, position, params.loadSize)
            val photo = response.results

            LoadResult.Page(
                data = photo,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (photo.isEmpty()) null else position + (params.loadSize / NETWORK_PAGE_SIZE_UNSPLASH)
            )

        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UnsplashPhoto>): Int? {

        return state.anchorPosition?.let { anchorPosition ->

            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}