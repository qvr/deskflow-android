/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
