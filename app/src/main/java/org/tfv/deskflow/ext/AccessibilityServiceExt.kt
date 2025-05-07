package org.tfv.deskflow.ext

import android.accessibilityservice.AccessibilityService
import kotlinx.serialization.Serializable

@Serializable
data class DumpSystemAction(val actionId: Int, val label: String)

fun AccessibilityService.systemActionsJson(): Map<String,DumpSystemAction> {
  val systemActions = systemActions

  val allJson = mutableMapOf<String,DumpSystemAction>()

  systemActions.forEach { action ->
    val json = DumpSystemAction( action.id, action.label.toString())

    allJson[action.id.toString()] = json
  }

  return allJson
}