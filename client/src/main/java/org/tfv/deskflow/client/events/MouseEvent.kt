package org.tfv.deskflow.client.events

import java.io.Serializable

data class MouseEvent(val type: Type, val id: UInt, val x: Int, val y: Int) : ClientEvent(), Serializable {
    enum class Type {
        Up,
        Down,
        Move,
        MoveRelative,
        Wheel
    }

    companion object {
        fun down(id: UInt) = MouseEvent(Type.Down, id,0,0)
        fun up(id: UInt) = MouseEvent(Type.Up, id,0,0)
        fun move(x: Int, y:Int) = MouseEvent(Type.Move,0u,x,y)
        fun moveRelative(x: Int, y:Int) = MouseEvent(Type.MoveRelative,0u,x,y)
        fun wheel(x: Int, y:Int) = MouseEvent(Type.Wheel,0u,x,y)
    }
}