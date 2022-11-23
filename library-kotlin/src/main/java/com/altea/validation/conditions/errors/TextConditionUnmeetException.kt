package com.altea.validation.conditions.errors

class TextConditionUnmeetException(
    override val message: String?,
    val originalInput: String?,
    val lastTransformedInput: String?
) : Exception(message)