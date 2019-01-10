package io.disassemble.asm.util

/**
 * @author Tyler Sedlar
 * @since 3/10/15
 */
object StringMatcher {

    fun matches(checker: String, threshold: String): Boolean {
        if (checker.length >= 2) {
            val selector = checker[0]
            val implier = checker[1]
            if (implier == '>') {
                val trimmed = checker.substring(2)
                when (selector) {
                    '*' -> {
                        return threshold.contains(trimmed)
                    }
                    '$' -> {
                        return threshold.endsWith(trimmed)
                    }
                    '!' -> {
                        return threshold != trimmed
                    }
                    '^' -> {
                        return threshold.startsWith(trimmed)
                    }
                    '~' -> {
                        return threshold.matches(trimmed.toRegex())
                    }
                    '-' -> {
                        return !threshold.contains(trimmed)
                    }
                    else -> {
                        return threshold == checker
                    }
                }
            }
        }
        return threshold == checker
    }
}
