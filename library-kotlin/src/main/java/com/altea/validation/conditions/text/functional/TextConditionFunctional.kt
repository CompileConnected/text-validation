package com.altea.validation.conditions.text.functional

import com.altea.validation.conditions.rules.TextRules
import com.altea.validation.conditions.text.TextCondition
import com.altea.validation.regex.PatternMatcher
import java.math.BigDecimal

private fun transformHint(hint: String?, target: String): String {
    return if (hint.isNullOrBlank()) {
        target
    } else {
        "$hint $target"
    }
}

fun CharSequence?.textRules(includeWhiteSpace: Boolean = true) = TextRules().apply {
    input(this@textRules)
    addCondition(
        condition = {
            if (includeWhiteSpace) {
                !it.isNullOrBlank()
            } else {
                it.isNullOrEmpty()
            }
        }, conditionErrorMessage = { _, hint ->
            transformHint(hint, "dibutuhkan")
        })
}

fun TextRules.between(intRange: IntRange, includeWhiteSpace: Boolean = true) = apply {
    this.addCondition(
        condition = TextCondition {
            val length = when {
                !includeWhiteSpace -> it.replace(" ", "")
                else -> it
            }.length
            length >= intRange.first && length <= intRange.last
        },
        conditionErrorMessage = { _, hint ->
            val m = when {
                !includeWhiteSpace -> "tanpa spasi"
                else -> "dengan spasi"
            }
            transformHint(hint, "diisi minimum ${intRange.first} dan maximum ${intRange.last} karakter $m")
        }
    )
}

fun TextRules.min(minimumChar: Int, includeWhiteSpace: Boolean = false) = apply {
    this.addCondition(
        condition = TextCondition {
            val length = when {
                !includeWhiteSpace -> it.replace(" ", "")
                else -> it
            }.length
            length >= minimumChar
        },
        conditionErrorMessage = { _, hint ->
            val m = when {
                !includeWhiteSpace -> "tanpa spasi"
                else -> "dengan spasi"
            }
            transformHint(hint, "diisi minimum $minimumChar karakter $m")
        }
    )
}

fun TextRules.mustOnly(mustOnlyChar: Int, includeWhiteSpace: Boolean = false) = apply {
    this.addCondition(
        condition = TextCondition {
            val length = when {
                !includeWhiteSpace -> it.replace(" ", "")
                else -> it
            }.length
            length == mustOnlyChar
        },
        conditionErrorMessage = { _, hint ->
            val m = when {
                !includeWhiteSpace -> "tanpa spasi"
                else -> "dengan spasi"
            }
            transformHint(hint, "diisi harus $mustOnlyChar karakter $m")
        }
    )
}


fun TextRules.max(maxOnlyChar: Int, includeWhiteSpace: Boolean = false) = apply {
    this.addCondition(
        condition = TextCondition {
            val length = when {
                !includeWhiteSpace -> it.replace(" ", "")
                else -> it
            }.length
            length <= maxOnlyChar
        },
        conditionErrorMessage = { _, hint ->
            val m = when {
                !includeWhiteSpace -> "tanpa spasi"
                else -> "dengan spasi"
            }
            transformHint(hint, "diisi maximum $maxOnlyChar karakter $m")
        }
    )
}

fun TextRules.between(minBigDecimal: BigDecimal, maxBigDecimal: BigDecimal) = apply {
    this.addCondition(
        condition = TextCondition {
            val cs = it.toBigDecimalOrNull() ?: BigDecimal.ZERO
            cs >= minBigDecimal && cs <= maxBigDecimal
        },
        conditionErrorMessage = { _, hint ->
            transformHint(hint, "diisi sejumlah minimum $minBigDecimal atau maximum $maxBigDecimal")
        }
    )
}

fun TextRules.max(maxBigDecimal: BigDecimal) = apply {
    this.addCondition(
        condition = TextCondition {
            val cs = it.toBigDecimalOrNull() ?: BigDecimal.ZERO
            cs <= maxBigDecimal
        },
        conditionErrorMessage = { _, hint ->
            transformHint(hint, "diisi maximum sejumlah $maxBigDecimal")
        }
    )
}

fun TextRules.min(minBigDecimal: BigDecimal) = apply {
    this.addCondition(
        condition = TextCondition {
            val cs = it.toBigDecimalOrNull() ?: BigDecimal.ZERO
            cs > minBigDecimal
        },
        conditionErrorMessage = { _, hint ->
            transformHint(hint, "diisi minimal sejumlah $minBigDecimal")
        }
    )
}

fun TextRules.alphaOrNumeric(includeWhiteSpace: Boolean = false) = apply {
    this.addCondition(
        condition = TextCondition { cs ->
            when {
                includeWhiteSpace -> cs.indexOfFirst { x -> !x.isLetterOrDigit() && !x.isWhitespace() } == -1
                else -> cs.indexOfFirst { x -> !x.isLetterOrDigit() } == -1
            }
        },
        conditionErrorMessage = { _, hint ->
            transformHint(hint, "hanya boleh diisi huruf atau kombinasi huruf dan angka")
        }
    )
}

fun TextRules.email() = apply {
    this.addCondition(
        condition = TextCondition { cs ->
            PatternMatcher.EMAIL_ADDRESS.matcher(cs).matches()
        },
        conditionErrorMessage = { _, _ ->
            "Email tidak valid"
        }
    )
}

fun TextRules.numberOnly(includeWhiteSpace: Boolean = false) = apply {
    this.addCondition(
        condition = TextCondition { cs ->
            when {
                includeWhiteSpace -> cs.indexOfFirst { x -> !x.isDigit() && !x.isWhitespace() } == -1
                else -> cs.indexOfFirst { x -> !x.isDigit() } == -1
            }
        },
        conditionErrorMessage = { _, hint ->
            transformHint(hint, "hanya boleh diisi angka")
        }
    )
}

fun TextRules.indonesiaPhoneNumber() = apply {
    this.addCondition(
        condition = TextCondition { cs ->
            PatternMatcher.INDONESIA_PHONE_NUMBER.matcher(cs).matches()
        },
        conditionErrorMessage = { _, _ ->
            "Nomor telepone tidak valid"
        }
    )
}
