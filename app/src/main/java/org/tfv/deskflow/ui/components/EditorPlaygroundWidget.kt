package org.tfv.deskflow.ui.components

// import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.tfv.deskflow.R
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot

@Composable
fun EditorPlaygroundWidget(
  appState: IAppState = LocalAppState.current,
  style: DeskflowCardStyle = deskflowCardWidgetStyleDefaults(),
) {
  val connState by appState.connectionStateFlow.collectAsStateWithLifecycle()
  val textFieldColors =
    TextFieldDefaults
      .colors( //        unfocusedContainerColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
      )
  var editorTextFieldValue by remember { mutableStateOf("") }
  DeskflowCardWidget(
    style =
      style,
    adjustStyleHeight = false,
    header = {
      Toolbar {
        Text(
          text = stringResource(R.string.editor_playground_widget_title),
          style = MaterialTheme.typography.titleLarge,
        )
        DeskflowFillSpacer()
      }
    },
  ) {
    Column(
      verticalArrangement =
        Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
    ) {
      DeskflowCardSubtitle(
        textRes = R.string.editor_playground_instructions,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
      )
      TextField(
        label = {
          Text(
            stringResource(R.string.editor_playground_field_label)
          )
        },
        value = editorTextFieldValue,
        colors = textFieldColors,
        onValueChange = { editorTextFieldValue = it },
        placeholder = {
          Text(
            stringResource(
              R.string.editor_playground_field_placeholder
            )
          )
        },

        modifier =
          Modifier
            .background(Color.Transparent)
            .onFocusChanged({state ->
              appState.setEditorPlaygroundFocused(state.isFocused)
            })
            .fillMaxWidth()
            .weight(1f),
      )
    }
  }
}

@Preview
@Composable
fun EditorPlaygroundWidgetPreview() {
  PreviewDeskflowThemedRoot { appState -> EditorPlaygroundWidget(appState) }
}
