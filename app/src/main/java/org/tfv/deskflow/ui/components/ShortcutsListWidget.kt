/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.tfv.deskflow.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.tfv.deskflow.R
import org.tfv.deskflow.components.GlobalKeyboardManager.Companion.loadKeyboardActions
import org.tfv.deskflow.services.keyboard.actions.VirtualKeyboardAction
import org.tfv.deskflow.types.EditorKeyboardAction
import org.tfv.deskflow.types.GlobalKeyboardAction
import org.tfv.deskflow.types.KeyboardAction
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot
import org.tfv.deskflow.ui.theme.LocalDeskflowExtendedColorScheme

@Composable
fun ShortcutsListWidget(
  appState: IAppState = LocalAppState.current,
  style: DeskflowCardStyle = deskflowCardWidgetStyleDefaults(),
) {

  val ctx = LocalContext.current
  val globalActions = remember {
    loadKeyboardActions<String, GlobalKeyboardAction>(
      ctx,
      R.raw.global_actions_defaults,
    )
  }

  val editorActions = remember {
    loadKeyboardActions<VirtualKeyboardAction, EditorKeyboardAction>(
      ctx,
      R.raw.editor_actions_defaults,
    )
  }

  // State for dropdown menu
  DeskflowCardWidget(
    style = style,

    header = {
      Toolbar {
        val extColorScheme = LocalDeskflowExtendedColorScheme.current

        Text(
          text = stringResource(R.string.shortcuts_list_widget_title),
          style = MaterialTheme.typography.titleLarge,
        )

        DeskflowFillSpacer()

        DeskflowIconButton(
          icon = {
            Icon(
              Icons.Default.Edit,
              contentDescription =
                stringResource(R.string.shortcuts_list_widget_toolbar_edit),
            )
          },
          onClick = {},
        )
      }
    },
  ) {
    val listState = rememberLazyListState()
    ScrollableVerticalBox(
      scrollState = listState,
      showGradients = true,
      showIcons = true,
      modifier = Modifier.fillMaxSize(),
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
      ) {
        val sectionTitleComponent = { resId: Int ->
          item {
            Text(
              text = stringResource(resId),
              style = MaterialTheme.typography.titleMedium,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).fillMaxWidth(),
            )
          }
          item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
        }

        val sectionItemsComponent = { actions: List<KeyboardAction<*>> ->
          items(actions) { action ->
            Row(
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth().padding(4.dp),
            ) {
              Text(
                text = action.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                modifier =
                  Modifier.weight(0.5f)
                    .padding(horizontal = 8.dp, vertical = 16.dp),
              )

              FlowRow(
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(0.5f).padding(8.dp),
              ) {
                if (action is EditorKeyboardAction) {
                  action.specialKey?.let { specialKey ->
                    Row(
                      modifier = Modifier.padding(4.dp),
                    ) {
                      Text(
                        text = specialKey.label,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        maxLines = 1,
                        modifier = Modifier.background(
                          SolidColor(Color.LightGray),
                          shape = RoundedCornerShape(4.dp),
                          alpha = 1f,
                        ).padding(horizontal = 8.dp, vertical = 8.dp),
                      )
                    }
                  }
                }
                action.defaultShortcutKeys.forEach { shortcutKey ->
                  Row(modifier = Modifier.padding(4.dp)) {
                    Text(
                      text = shortcutKey.label,
                      style = MaterialTheme.typography.labelLarge,
                      textAlign = TextAlign.Center,
                      color = Color.Black,
                      maxLines = 1,
                      modifier =
                        Modifier.background(
                          SolidColor(Color.LightGray),
                          shape = RoundedCornerShape(4.dp),
                          alpha = 1f,
                        )
                          .padding(horizontal = 8.dp, vertical = 8.dp),
                    )
                  }
                }
              }
            }
          }
        }
        sectionTitleComponent(R.string.shortcuts_list_widget_global_actions)
        sectionItemsComponent(globalActions.values.toList())

        sectionTitleComponent(R.string.shortcuts_list_widget_editor_actions)
        sectionItemsComponent(editorActions.values.toList())

      }
    }
  }
}

@Preview
@Composable
fun ShortcutsListWidgetPreview() {
  PreviewDeskflowThemedRoot { appState ->
    ShortcutsListWidget(appState)
  }
}
