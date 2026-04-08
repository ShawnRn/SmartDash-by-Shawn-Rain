package com.shawnrain.sdash.ui.text

object DisplayTextFormatter {
    private val hanAsciiBoundary = Regex("(?<=[\\p{IsHan}])(?=[A-Za-z0-9])|(?<=[A-Za-z0-9])(?=[\\p{IsHan}])")
    private val multiSpace = Regex(" {2,}")

    fun applyCjkSpacing(text: String): String {
        return text
            .replace(hanAsciiBoundary, " ")
            .replace(multiSpace, " ")
            .trim()
    }
}

fun String.withDisplaySpacing(): String = DisplayTextFormatter.applyCjkSpacing(this)
