package com.cleanai.webinfra

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String
)
