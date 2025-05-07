package org.tfv.deskflow.client.util

data class Bitmask(val value: Int = 0) {

  fun set(bit: Int): Bitmask {
    return copy(value = value or (1 shl bit))
  }

  fun clear(bit: Int):Bitmask {
    return copy(value = value and (1 shl bit).inv())
  }

  fun toggle(bit: Int):Bitmask {
    return copy(value = value xor (1 shl bit))
  }

  fun isSet(bit: Int): Boolean {
    return value and (1 shl bit) != 0
  }

  override fun toString(): String = "Bitmask(value=${value.toString(2).padStart(32, '0')})"
}