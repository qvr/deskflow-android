package org.tfv.deskflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun LifecycleEventHookEffect(vararg filterEventTypes: Lifecycle.Event, fireImmediate: Boolean = false, onEvent: (Lifecycle.Event?) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, eventType ->
            if (filterEventTypes.isEmpty() || filterEventTypes.contains(eventType)) {
                onEvent(eventType)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (fireImmediate) {
        onEvent(null)
    }
}