package com.rohitthebest.manageyourrenters.data.filter

import java.io.Serializable

enum class StringFilterOptions : Serializable {

    startsWith,
    endsWith,
    containsWith,
    regex
}