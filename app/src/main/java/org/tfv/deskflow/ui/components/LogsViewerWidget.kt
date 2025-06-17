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

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.oshai.kotlinlogging.Level
import java.lang.Math.random
import java.text.SimpleDateFormat
import kotlinx.coroutines.delay
import org.tfv.deskflow.R
import org.tfv.deskflow.logging.LogRecordEvent
import org.tfv.deskflow.ui.annotations.PreviewAll
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot
import org.tfv.deskflow.ui.theme.DimensionDefaults
import org.tfv.deskflow.ui.theme.LocalDeskflowExtendedColorScheme

@Composable
fun LogsViewerWidget(
  appState: IAppState = LocalAppState.current,
  style: DeskflowCardStyle = deskflowCardWidgetStyleDefaults(),
) {
  // State for dropdown menu
  var filterExpanded by remember { mutableStateOf(false) }
  var selectedLevel by remember { mutableStateOf(Level.INFO) }
  var autoScrollEnabled by remember { mutableStateOf(true) }

  DeskflowCardWidget(
    style = style,
    header = {
      Toolbar {
        val extColorScheme = LocalDeskflowExtendedColorScheme.current

        DeskflowCardWidgetTitle(
          textResId = R.string.log_widget_title
        )

        DeskflowFillSpacer()

        DeskflowIconButton(
          icon = {
            Icon(
              Icons.Default.Delete,
              contentDescription =
                stringResource(R.string.log_widget_toolbar_clear_logs),
            )
          },
          onClick = {
            appState.clearLogRecords()
            autoScrollEnabled = true
          },
        )

        DeskflowToggleIconButton(
          checked = autoScrollEnabled,
          icon = { isChecked ->
            Icon(
              if (isChecked) Icons.Outlined.KeyboardArrowDown
              else Icons.Filled.KeyboardArrowDown,
              contentDescription =
                stringResource(R.string.log_widget_toolbar_scroll_to_bottom),
            )
          },
          onCheckChange = { autoScrollEnabled = it },
        )

        FilledTonalButton(
          onClick = { filterExpanded = true },
          contentPadding = PaddingValues(1.dp),
          shape = RoundedCornerShape(4.dp),
          modifier =
            Modifier.padding(0.dp)
              .height(DimensionDefaults.toolbarButtonHeight)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp).padding(end = 0.dp, start = 8.dp),
          ) {
            Text(
              text = selectedLevel.name,
              modifier = Modifier,
              style =
                MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  //color = extColorScheme.onToolbar,
                ),
            )
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription =
                stringResource(R.string.log_widget_toolbar_filter_logs_label),
            )
            DropdownMenu(
              expanded = filterExpanded,
              onDismissRequest = { filterExpanded = false },
            ) {
              Level.entries.forEach { level ->
                DropdownMenuItem(
                  text = { Text(level.name) },
                  onClick = {
                    selectedLevel = level
                    filterExpanded = false
                  },
                )
              }
            }
          }
        }
      }
    },
  ) { style ->
    BoxWithConstraints {
      LogTailView(
        level = selectedLevel,
        autoScrollEnabled = autoScrollEnabled,
        modifier = style.contentModifier.height(maxHeight).width(maxWidth),
      )
    }
  }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun LogTailView(
  level: Level,
  autoScrollEnabled: Boolean,
  appState: IAppState = LocalAppState.current,
  modifier: Modifier = Modifier.fillMaxSize(),
) {
  // Apply filtering
  val allLogs: List<LogRecordEvent> by
    appState.logRecordsFlow.collectAsStateWithLifecycle()
  val logs = remember(allLogs, level) { allLogs.filter { it.level >= level } }
  val listState = rememberLazyListState()

  // Only auto-scroll when enabled
  LaunchedEffect(logs.size, autoScrollEnabled) {
    if (autoScrollEnabled) {
      listState.animateScrollToItem(logs.size.coerceAtLeast(1) - 1)
    }
  }
  ScrollableVerticalBox(listState) {
    LazyColumn(
      state = listState,
      verticalArrangement =
        if (logs.isEmpty()) Arrangement.Center else Arrangement.Top,
      modifier = modifier.fillMaxHeight(),
    ) {
      when {
        logs.isEmpty() -> {
          item {
            Text(
              text = stringResource(R.string.log_widget_no_logs),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.fillMaxSize().padding(16.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
          }
        }

        else -> {
          val timeFormat = SimpleDateFormat("HH:mm:ss.SSS")
          items(logs) { record ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth().padding(4.dp),
            ) {
              Text(
                text = timeFormat.format(record.timestamp), // .toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(4.dp),
              )
              Text(
                text = record.level.name,
                textAlign = TextAlign.Center,
                color =
                  when (record.level) {
                    Level.ERROR -> MaterialTheme.colorScheme.onError
                    Level.WARN ->
                      LocalDeskflowExtendedColorScheme.current.onWarning
                    Level.INFO -> MaterialTheme.colorScheme.onPrimary
                    Level.DEBUG -> MaterialTheme.colorScheme.onSecondary
                    Level.TRACE -> MaterialTheme.colorScheme.onTertiary
                    else -> Color.White
                  },
                style =
                  MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                  ),
                modifier =
                  Modifier.background(
                      when (record.level) {
                        Level.ERROR -> MaterialTheme.colorScheme.error
                        Level.WARN ->
                          LocalDeskflowExtendedColorScheme.current.warning
                        Level.INFO -> MaterialTheme.colorScheme.primary
                        Level.DEBUG -> MaterialTheme.colorScheme.secondary
                        Level.TRACE -> MaterialTheme.colorScheme.tertiary
                        else -> Color.Black
                      },
                      shape = RoundedCornerShape(4.dp),
                    )
                    .width(50.dp)
                    .padding(4.dp),
              )
              Text(
                text = record.tag,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style =
                  MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                  ),
                maxLines = 1,
                overflow = TextOverflow.StartEllipsis,
                modifier = Modifier.padding(4.dp)
                  .width(100.dp),
              )
              Text(
                text = record.message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(4.dp),
              )
            }
          }
        }
      }
    }
  }
}


@PreviewAll
@Composable
fun LogTailViewPreview() {
  val levels = Level.entries
  val levelRange = IntRange(0, levels.size - 1)
  PreviewDeskflowThemedRoot { appState ->
      LaunchedEffect(Unit) {
        if (appState is PreviewAppState) {
          repeat(1000) {
            delay(200)
            val level = levels[levelRange.random()]
            appState.addLogRecord(
              LogRecordEvent(
                tag = "LogTailViewPreview",
                message = "Log message #$it",
                level = level,
                timestamp = System.currentTimeMillis(),
              )
            )
          }
        }
      }

      LogsViewerWidget(appState)
    }

}
