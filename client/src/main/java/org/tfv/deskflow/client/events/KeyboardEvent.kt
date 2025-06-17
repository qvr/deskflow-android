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

package org.tfv.deskflow.client.events

import org.tfv.deskflow.client.models.keys.KeyModifierMask
import java.io.Serializable

data class KeyboardEvent(val type: Type, val id: UInt, val button: UInt = 0u, val mask: UInt, val count: Short = 0) : ClientEvent(), Serializable {

    enum class Type {
        Up,
        Down,
        Repeat
    }

    fun getModifiers():KeyModifierMask {
        return KeyModifierMask(mask)
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "KeyboardEvent(type=$type, id=($id,${id.toHexString()},${id.toString(2)}), button=$button, mask=($mask,${mask.toHexString()},${mask.toString(2)}), count=$count)"
    }

    companion object {
        fun down(id: UInt, button: UInt = 0u, mask: UInt = 0u) = KeyboardEvent(Type.Down, id, button, mask)
        fun up(id: UInt, button: UInt = 0u, mask: UInt = 0u) = KeyboardEvent(Type.Up, id, button, mask)
        fun repeat(id: UInt, button: UInt = 0u, mask: UInt = 0u, count: Short = 0) = KeyboardEvent(Type.Repeat, id, button, mask,count)
    }
}