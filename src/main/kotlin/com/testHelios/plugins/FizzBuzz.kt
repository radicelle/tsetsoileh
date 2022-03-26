package com.testHelios.plugins

import com.testHelios.plugins.StringOrInt.Integer

object FizzBuzz {

    /**
     * Maps values to StringOrInt.Integer then replace by first sequence, then replace by second sequence
     */
    fun listReplacement(int1: Int, int2: Int, limit: Int, str1: String, str2: String): List<String> =
        (1..limit).asSequence().map { Integer(it) }
            .map { valueInList -> replaceIfMultiple(int1, valueInList, str1) }.map { replaceIfMultiple(int2, it, str2) }
            .map { it.toString() }.toList()


    /**
     * When the current value is a multiple of the integer in first param then it is replaced with the replacement string
     * Though if it has already been replaced it is concatenated as follows : ${currentValue}${replacement}
     */
    private fun replaceIfMultiple(integer: Int, currentValue: StringOrInt, replacement: String): StringOrInt {
        return when (currentValue) {
            is Integer -> replaceIfModuloEquals0(currentValue.int, integer, replacement)
            is StringOrInt.ReplacedString -> StringOrInt.ReplacedString(
                currentValue.value,
                currentValue.concat(replaceIfModuloEquals0(currentValue.value, integer, replacement))
            )
        }
    }

    /**
     * In case modulo equals 0 then converts to ReplacedString and sets the replacement String value
     * Otherwise returns the int value encapsulated in StringOrInt to fit the mapping process
     */
    private fun replaceIfModuloEquals0(
        currentValue: Int,
        integer: Int,
        replacement: String
    ): StringOrInt {
        return when (currentValue % integer) {
            0 -> StringOrInt.ReplacedString(currentValue, replacement)
            else -> Integer(currentValue)
        }
    }

}


/**
 * Sealed class which represents either a String or an Int
 * Int will be defined as Integer to ease distinguish it whe Kotlin Int
 * String will be defined as ReplacedString because it is the entity which Integer is replaced with during analysis
 *
 * Then it can be used as a primitive type for functional programming
 */
sealed class StringOrInt {
    class Integer(val int: Int) : StringOrInt()
    class ReplacedString(val value: Int, val string: String) : StringOrInt() {
        fun concat(other: StringOrInt): String = when (other) {
            is Integer -> this.string
            is ReplacedString -> "${this.string}${other.string}"
        }
    }

    override fun toString(): String =
        when (this) {
            is Integer -> this.int.toString()
            is ReplacedString -> this.string
        }
}