package com.rohitthebest.manageyourrenters.data

enum class DocumentType {

    PDF,
    IMAGE,
    URL
}

data class SupportingDocument(
    var documentName: String,
    var documentUrl: String,
    var documentType: DocumentType
) {

    constructor() : this(

        "",
        "",
        DocumentType.URL
    )
}
