package org.tfv.deskflow.client.util

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
abstract class AbstractDisposable : Disposable, AutoCloseable {

    private var disposed = AtomicBoolean(false)
    private val disposeLock = Any()
    override val isDisposed: Boolean
        get() = disposed.load()

    override fun dispose() {
        if (disposed.load()) return
        synchronized(disposeLock) {
            if (disposed.exchange(true))
                return

            onDispose()
        }
    }

    abstract fun onDispose()

    override fun close() {
        dispose()
    }
}

