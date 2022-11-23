package com.altea.validation.conditions.text

fun interface TextCondition : TextNullableCondition {
    fun validCondition(input: String): Boolean

    override fun valid(input: String?): Boolean {
        return try {
            validCondition(input!!)
        } catch (e: NullPointerException) {
            false
        }
    }
}