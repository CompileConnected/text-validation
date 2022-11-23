package com.altea.validation.regex

import java.util.regex.Pattern

object PatternMatcher {

    @JvmStatic
    val EMAIL_ADDRESS = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    @JvmStatic
    val INDONESIA_PHONE_NUMBER = Pattern.compile(
        "^(?:\\+62|\\(0\\d{2,3}\\)|0)\\s?(?:361|8[17]\\s?\\d?)?(?:[ \\-]?\\d{3,4}){2,3}\$"
    )
}