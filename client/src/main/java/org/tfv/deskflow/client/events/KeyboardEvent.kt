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