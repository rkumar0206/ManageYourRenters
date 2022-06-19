package com.rohitthebest.manageyourrenters.data

import java.io.Serializable

enum class DocumentType {

    PDF,
    IMAGE,
    URL
}

data class SupportingDocument(
    var documentName: String,
    var documentUrl: String,
    var documentType: DocumentType
) : Serializable {

    constructor() : this(

        "",
        "",
        DocumentType.URL
    )
}
