@file:Suppress("UNCHECKED_CAST")

package org.tfv.deskflow.client.ext

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Reflectively builds a string of all public properties on the instance.
 */
inline fun <reified T : Any> T.toDebugString(): String {
    val klazz = this::class

    val props = klazz.memberProperties as Collection<KProperty1<T, *>>
    return props.joinToString(
        prefix = "${klazz.simpleName}(",
        postfix = ")"
    ) { prop ->
        // prop.getter.call(this) also works
        val value = prop.get(this)
        "${prop.name}=$value"
    }
}