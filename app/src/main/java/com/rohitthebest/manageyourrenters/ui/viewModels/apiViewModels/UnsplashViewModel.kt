package com.rohitthebest.manageyourrenters.ui.viewModels.apiViewModels

import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.rohitthebest.manageyourrenters.api.unsplash.UnsplashRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnsplashViewModel @Inject constructor(
    private val repository: UnsplashRepository
) : ViewModel() {

    //for searching image
    private val currentQuery = MutableLiveData<String>()

    val unsplashSearchResult = currentQuery.switchMap {

        repository.getSearchResultsFromUnsplash(it).asLiveData().cachedIn(viewModelScope)
    }

    fun searchImage(query: String) {

        currentQuery.value = query
    }
}