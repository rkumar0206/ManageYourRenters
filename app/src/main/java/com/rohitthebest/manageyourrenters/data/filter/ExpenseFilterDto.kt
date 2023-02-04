package com.rohitthebest.manageyourrenters.data.filter

import java.io.Serializable

data class ExpenseFilterDto(
    var isPaymentMethodEnabled: Boolean,
    var isAmountEnabled: Boolean,
    var isSpentOnEnabled: Boolean,
    var isSortByEnabled: Boolean,
    var paymentMethods: List<String>,
    var selectedAmountFilter: IntFilterOptions,
    var amount: Double,
    var amount2: Double,        // only used when selected INtFilterOption is isBetween
    var selectedSpentOnFilter: StringFilterOptions,
    var spentOnText: String,
    var sortByFilter: SortFilter,
    var sortOrder: SortOrder
) : Serializable {

    constructor() : this(
        false,
        false,
        false,
        true,
        emptyList(),
        IntFilterOptions.isEqualsTo,
        0.0,
        0.0,
        StringFilterOptions.containsWith,
        "",
        SortFilter.dateCreated,
        SortOrder.descending
    )
}

