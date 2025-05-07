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