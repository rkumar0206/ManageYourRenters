package com.rohitthebest.manageyourrenters.data

data class SupportingDocumentHelperModel(
    var modelName: String,
    var documentType: DocumentType,
    var documentName: String,
    var documentUri: String? = null, // for document type image or pdf
    var documentUrl: String = "" // for document type url
) {

    constructor() : this(
        "",
        DocumentType.PDF,
        "",
        null,
        ""
    )
}