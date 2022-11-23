package com.altea.validation.conditions.text

fun interface TextConditionErrorMessage {
    fun errorMessage(input: String?, hint: String?): String
}