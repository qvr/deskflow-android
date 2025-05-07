package org.tfv.deskflow.client.models

enum class ClipboardDataMarker(val code: Int) {
  Unknown(0),
  Start(1),
  Data(2),
  End(3),
}