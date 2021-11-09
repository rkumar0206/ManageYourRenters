package com.rohitthebest.manageyourrenters.api.unsplash

import com.rohitthebest.manageyourrenters.data.UnsplashPhoto


data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)