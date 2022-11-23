package com.altea.validation.conditions.rules

import com.altea.validation.conditions.DependsOnCondition
import com.altea.validation.conditions.errors.TextConditionNoMessageException
import com.altea.validation.conditions.errors.TextConditionUnmeetException
import com.altea.validation.conditions.text.TextConditionErrorMessage
import com.altea.validation.conditions.text.TextNullableCondition

class TextRules(
    input: String? = null,
    hint: String? = null,
    conditions: List<Pair<TextNullableCondition, TextConditionErrorMessage>> = listOf()
) {
    private var originalInput = input
    private var mHint = hint
    private var mConditions = mutableListOf<ValidationCondition>()

    init {
        mConditions.add(
            ValidationCondition(
                condition = {
                    true
                },
                conditionErrorMessage = { _, _ ->
                    "initial condition"
                },
                transformedInput = input
            )
        )
        mConditions += conditions.map {
            ValidationCondition(
                condition = it.first,
                conditionErrorMessage = it.second
            )
        }
    }


    private var cacheInput: CharSequence? = null

    private var prevValidationStatus: ValidationStatus? = null

    private var alwaysRequiredErrorMessage = true

    private data class ValidationCondition(
        val condition: TextNullableCondition,
        var conditionErrorMessage: TextConditionErrorMessage,
        var dependsOnCondition: DependsOnCondition? = null,
        var transformedInput: String? = null
    )

    private data class ValidationStatus(
        val condition: TextNullableCondition?,
        val conditionErrorMessage: TextConditionErrorMessage?,
        val isValid: Boolean,
        val input: String?,
        val recordTransformedInput: List<Pair<Int, String>>?
    ) {
        fun getLastTransformedInput() = recordTransformedInput?.lastOrNull()?.second
    }

    private inline val lastCondition
        get() = mConditions[mConditions.lastIndex]

    /**
     * change last depends on condition
     **/
    fun dependsOnCondition(dependsOn: DependsOnCondition?) = apply {
        lastCondition.dependsOnCondition = dependsOn
    }

    /**
     * change last condition error message
     **/
    fun changeErrorMessage(message: String) = changeErrorMessage { _, _ ->
        message
    }

    /**
     * change last condition error message
     */
    fun changeErrorMessage(conditionErrorMessage: TextConditionErrorMessage) = apply {
        lastCondition.conditionErrorMessage = conditionErrorMessage
    }

    fun addConditionDependsOn(
        dependsOn: DependsOnCondition,
        condition: TextNullableCondition,
        conditionErrorMessage: TextConditionErrorMessage
    ) = apply {
        mConditions.add(
            ValidationCondition(
                condition = condition,
                conditionErrorMessage = conditionErrorMessage,
                dependsOnCondition = dependsOn
            )
        )
    }

    fun addConditionDependsOn(
        dependsOn: DependsOnCondition,
        condition: TextNullableCondition,
        conditionErrorMessage: String
    ) = apply {
        mConditions.add(
            ValidationCondition(
                condition = condition,
                conditionErrorMessage = { _, _ ->
                    conditionErrorMessage
                },
                dependsOnCondition = dependsOn
            )
        )
    }

    fun addCondition(condition: TextNullableCondition, conditionErrorMessage: String) = apply {
        mConditions.add(ValidationCondition(
            condition = condition,
            conditionErrorMessage = { _, _ ->
                conditionErrorMessage
            }
        ))
    }

    fun addCondition(condition: TextNullableCondition, conditionErrorMessage: TextConditionErrorMessage) = apply {
        mConditions.add(
            ValidationCondition(
                condition = condition,
                conditionErrorMessage = conditionErrorMessage,
            )
        )
    }

    fun input(input: CharSequence?) = apply {
        this.originalInput = input?.toString()
    }

    /**
     * revert transformed input to original input
     */
    fun transformToOriginalInput() = apply {
        lastCondition.transformedInput = originalInput
    }

    /**
     * transformed input to any desired string
     *
     * this transformed input will keep on until it revert back to original
     */
    fun transformInput(transform: (origin: String) -> String) = apply {
        val currentInputTrans = mConditions
            .findLast { it.transformedInput != null }
            ?.transformedInput
            ?: originalInput
            ?: ""

        val nextInputTrans = transform.invoke(currentInputTrans)
        lastCondition.transformedInput = nextInputTrans
    }

    fun hint(message: String) = apply {
        this.mHint = message
    }

    fun enableErrorMessage() = apply {
        alwaysRequiredErrorMessage = true
    }

    fun disableErrorMessage() = apply {
        alwaysRequiredErrorMessage = false
    }

    private fun runValidationStatus(): ValidationStatus {
        val originInput = originalInput
        if (cacheInput == originInput && !cacheInput.isNullOrBlank() && !originInput.isNullOrBlank()) {
            return prevValidationStatus!!
        }
        cacheInput = originInput

        var nextTransformedInput = originInput
        val recordTransformedInput = mutableListOf<Pair<Int, String>>()
        var index = 0
        val errorCondition = this.mConditions.find {
            if (it.transformedInput != null) {
                nextTransformedInput = it.transformedInput
                recordTransformedInput.add(index to nextTransformedInput!!.toString())
            }

            ++index

            val concreteInput = nextTransformedInput
            val isValid = when {
                it.dependsOnCondition == null -> !it.condition.valid(concreteInput)
                it.dependsOnCondition!!.depend() -> !it.condition.valid(concreteInput)
                else -> false
            }
            isValid
        }
        val nextValidationStatus = ValidationStatus(
            condition = errorCondition?.condition,
            conditionErrorMessage = errorCondition?.conditionErrorMessage,
            isValid = errorCondition == null,
            input = originalInput,
            recordTransformedInput = recordTransformedInput
        )
        prevValidationStatus = nextValidationStatus
        return nextValidationStatus
    }

    private inline val finalizeValidationStatus: ValidationStatus
        @Throws(
            TextConditionNoMessageException::class,
            TextConditionUnmeetException::class
        )
        get() {
            val status = runValidationStatus()
            if (!status.isValid) {
                val lastTransformedInput = status.getLastTransformedInput()
                val lastInput = lastTransformedInput ?: status.input ?: "UNDEFINED INPUT"
                val errorMessage = status.conditionErrorMessage?.errorMessage(lastInput, mHint)
                if (alwaysRequiredErrorMessage) {
                    if (errorMessage.isNullOrBlank()) {
                        throw TextConditionNoMessageException()
                    } else {
                        throw TextConditionUnmeetException(
                            message = errorMessage,
                            originalInput = originalInput,
                            lastTransformedInput = lastInput
                        )
                    }
                }
            }
            return status
        }

    @Throws(
        TextConditionNoMessageException::class,
        TextConditionUnmeetException::class
    )
    fun validOrThrow(): Boolean {
        return finalizeValidationStatus.isValid
    }

    fun valid() = try {
        validOrThrow()
    } catch (e: Exception) {
        false
    }

    @Throws(
        TextConditionUnmeetException::class,
        TextConditionNoMessageException::class,
        NullPointerException::class
    )
    fun valueOrThrow(): String {
        return finalizeValidationStatus.input!!
    }

    fun value() = try {
        valueOrThrow()
    } catch (e: Exception) {
        null
    }
}