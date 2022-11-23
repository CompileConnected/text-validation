package com.altea.validation.conditions.text

fun interface TextNullableCondition {
    fun valid(input: String?): Boolean
}