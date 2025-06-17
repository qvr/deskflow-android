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

package org.tfv.deskflow.client.models.keys

import org.tfv.deskflow.client.util.Keyboard

data class KeyModifierMask(val mask: UInt) {

    val isShift = testModifier(mask, Keyboard.ModifierKeyMask.Shift)
    val isControl = testModifier(mask, Keyboard.ModifierKeyMask.Control)
    val isAlt = testModifier(mask, Keyboard.ModifierKeyMask.Alt)
    val isMeta = testModifier(mask, Keyboard.ModifierKeyMask.Meta)
    val isSuper = testModifier(mask, Keyboard.ModifierKeyMask.Super)
    val isAltGr = testModifier(mask, Keyboard.ModifierKeyMask.AltGr)
    val isLevel5Lock = testModifier(mask, Keyboard.ModifierKeyMask.Level5Lock)
    val isCapsLock = testModifier(mask, Keyboard.ModifierKeyMask.CapsLock)
    val isNumLock = testModifier(mask, Keyboard.ModifierKeyMask.NumLock)
    val isScrollLock = testModifier(mask, Keyboard.ModifierKeyMask.ScrollLock)


    companion object {
        private fun testModifier(mask: UInt, mod: Keyboard.ModifierKeyMask): Boolean {
            return mask.shr(mod.bit).and(1u) == 1u
        }
    }
}