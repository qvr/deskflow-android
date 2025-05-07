package org.tfv.deskflow.client.io

fun String.scanf(format: String): List<Any> {
    val specifiers = Regex("%[0-9]?[bsi(ldfc)]").findAll(format).map { it.value }.toList()

    if (specifiers.count { it == "%s" } > 1 || (specifiers.lastOrNull() != "%s" && "%s" in specifiers))
        throw IllegalArgumentException("Only one %s is allowed, and it must be at the end")

    val tokens = this.trim().split(Regex("\\s+"), limit = if ("%s" in specifiers) specifiers.size else -1)

    if (tokens.size < specifiers.size)
        throw IllegalArgumentException("Input has fewer tokens than format specifiers")

    return specifiers.mapIndexed { i, spec ->
        val value = tokens[i]
        when (spec) {
            "%b" -> value.toByte()
            "%i" -> value.toInt()
            "%l" -> value.toLong()
            "%f" -> value.toFloat()
            "%d" -> value.toDouble()
            "%c" -> value.single()
            "%s" -> value
            else -> throw IllegalArgumentException("Unsupported format specifier: $spec")
        }
    }
}
