package com.rohitthebest.manageyourrenters.data.filter

import java.io.Serializable

enum class IntFilterOptions : Serializable {

    isLessThan,
    isGreaterThan,
    isBetween,
    isEqualsTo
}