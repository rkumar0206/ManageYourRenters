package com.rohitthebest.manageyourrenters.api.unsplash

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.rohitthebest.manageyourrenters.others.Constants.NETWORK_PAGE_SIZE_UNSPLASH
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnsplashRepository @Inject constructor(
    private val unsplashApi: UnsplashAPI
) {

    fun getSearchResultsFromUnsplash(query: String) = Pager(

        config = PagingConfig(
            pageSize = NETWORK_PAGE_SIZE_UNSPLASH,
            maxSize = 100
        ),
        pagingSourceFactory = { UnsplashPagingSource(unsplashApi, query) }
    ).flow

}