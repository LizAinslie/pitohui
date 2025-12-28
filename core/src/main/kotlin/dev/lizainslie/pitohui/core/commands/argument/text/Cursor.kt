package dev.lizainslie.pitohui.core.commands.argument.text

/**
 * Class representing an iteration position over a given string, with
 * convenience functions.
 *
 * @param input Input string to iterate over. Never modified.
 */
class Cursor(public val input: String) {
    /**
     * Current iteration index, starting at `-1`.
     */
    var index: Int = -1

    /**
     * Returns `true` if there are more characters left to iterate over.
     */
    val hasNext: Boolean
        get() = index < input.length - 1

    /**
     * Returns `true` if there are characters to iterate backwards to.
     */
    val hasPrevious: Boolean
        get() = index > 0

    fun consumeNumber(amount: Int): String =
        buildString {
            var total = 0

            while (hasNext && total < amount) {
                append(next())

                total += 1
            }
        }

    /**
     * Iterate over the rest of the string, returning the result.
     */
    fun consumeRemaining(): String =
        buildString {
            while (hasNext) append(next())
        }

    /**
     * Iterate over the rest of the string as long as the predicate returns
     * `true`, returning the result.
     */
    fun consumeWhile(predicate: (Char) -> Boolean): String? {
        var result: String? = null

        while (hasNext && predicate(peekNext()!!))
            result = (result ?: "") + next()

        return result
    }

    /**
     * Skip any immediate whitespace, updating the [index].
     */
    fun skipWhitespace(): Boolean {
        if (peekNext() != ' ') return false
        while (peekNext() == ' ') next()

        return true
    }

    /**
     * Increment the [index] and return the character found there, throwing if
     * we're at the end of the string.
     */
    fun next(): Char {
        if (hasNext) {
            index += 1
            return input[index]
        }

        error("Cursor has no further elements.")
    }

    /**
     * Increment the [index] and return the character found there, or `null` if
     * we're at the end of the string.
     */
    fun nextOrNull(): Char? {
        if (hasNext) {
            index += 1
            return input[index]
        }

        return null
    }

    /**
     * Decrement the [index] and return the character found there, throwing if
     * we're at the start of the string.
     */
    fun previous(): Char {
        if (hasPrevious) {
            index -= 1
            return input[index]
        }

        error("Cursor has no previous elements.")
    }

    /**
     * Decrement the [index] and return the character found there, or `null` if
     * we're at the start of the string.
     */
    fun previousOrNull(): Char? {
        if (hasPrevious) {
            index -= 1
            return input[index]
        }

        return null
    }

    /**
     * Return the character at the current index.
     */
    fun peek(): Char = input[index]

    /**
     * Return the character at the next index, or `null` if we're at the end of
     * the string.
     */
    fun peekNext(): Char? {
        if (hasNext) return input[index + 1]
        return null
    }

    /**
     * Return the character at the previous index, or `null` if we're at the
     * start of the string.
     */
    fun peekPrevious(): Char? {
        if (hasPrevious) return input[index - 1]
        return null
    }
}