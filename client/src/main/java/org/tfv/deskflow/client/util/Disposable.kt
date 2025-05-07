package org.tfv.deskflow.client.util

import arrow.core.Option

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}

fun <T : Disposable> disposeOf(resource: T?): T? {
    Option.fromNullable(resource)
      .onSome { it -> it.dispose() }
    
    return null
}