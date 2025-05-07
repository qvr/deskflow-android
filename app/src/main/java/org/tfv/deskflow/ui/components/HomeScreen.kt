@file:OptIn(ExperimentalLayoutApi::class, ExperimentalLayoutApi::class)

package org.tfv.deskflow.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlin.math.ceil
import kotlinx.serialization.Serializable
import org.tfv.deskflow.client.util.logging.KLoggingManager
import org.tfv.deskflow.ext.drawTopShadow
import org.tfv.deskflow.ui.components.preview.PreviewAppState
import org.tfv.deskflow.ui.components.preview.PreviewDeskflowThemedRoot
import kotlin.math.max

private val log = KLoggingManager.logger("HomeScreen")

@Serializable data object HomeScreenRoute

fun NavController.navigateToHomeScreen(navOptions: NavOptions) =
  navigate(route = HomeScreenRoute, navOptions)

fun NavGraphBuilder.homeScreen(appState: IAppState) {
  composable<HomeScreenRoute> { HomeScreen() }
}

@Composable
fun HomeScreen(appState: IAppState = LocalAppState.current) {

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isEditorPlaygroundFocused by appState.editorPlaygroundFocused.collectAsStateWithLifecycle()
    val width = maxWidth.value
    val height = maxHeight.value

    val (isPortrait, isXL, isLarge) = currentDeviceConfig()
    val spacing = 8.dp

    val widgetHeightWithItems = { count: Int ->
      val height = ((height - (spacing.value * (count - 1))) / count)
      if (!isEditorPlaygroundFocused)
        height
      else
        max(400f,height)
    }

    val maxItemsPerRow =
      when {
        isPortrait && isXL -> 2
        isPortrait -> 1
        isXL -> 2
        isLarge -> 2
        else -> 2
      }

    val widgetRowCount = ceil(3f / maxItemsPerRow.toDouble())
    val widgetHeight =
      when {
        isPortrait && isXL -> widgetHeightWithItems(2)
        isPortrait -> widgetHeightWithItems(1)
        isXL -> widgetHeightWithItems(2)
        else -> widgetHeightWithItems(1)
      }

    val widgetWidth =
      ((width  - (spacing.value * (maxItemsPerRow - 1))) / maxItemsPerRow)

    val scrollState = rememberScrollState()
    FlowRow(
      horizontalArrangement =
        Arrangement.spacedBy(
          space = spacing,
          alignment = Alignment.CenterHorizontally,
        ),
      verticalArrangement =
        Arrangement.spacedBy(
          space = spacing,
          alignment = Alignment.Top,
        ),
      maxItemsInEachRow = maxItemsPerRow,
      modifier =
        Modifier
          .height(maxHeight)
          .width(maxWidth)
          .verticalScroll(scrollState)
          .animateContentSize()
          .drawTopShadow(),
    ) {
      val widgetStyle = { fillWidth: Boolean, fillHeight: Boolean ->
        val mod = when {
          fillHeight -> Modifier.height(widgetHeight.dp)
          else -> Modifier.wrapContentHeight(unbounded = false)
        }

        deskflowCardWidgetStyleDefaults(
          contentModifier = Modifier,
          containerModifier = when {
            fillWidth -> mod.fillMaxWidth()

            else -> mod.width(widgetWidth.dp)
          },
        )
      }

      val editorPlaygroundStyle = {
        deskflowCardWidgetStyleDefaults(
          contentModifier = Modifier,
          containerModifier = Modifier
            .width(widgetWidth.dp)
            .let { mod ->
              if (isEditorPlaygroundFocused)
                mod.height( widgetHeight.dp)
              else
                mod.weight(1f, true)
            }
        )
      }
      ShortcutsListWidget(style = widgetStyle(false, true))
      Column(
        modifier = Modifier.height(widgetHeight.dp)
      ) {
        ConnectionStatusWidget(style = widgetStyle(false, false))
        EditorPlaygroundWidget(style = editorPlaygroundStyle())
      }
      LogsViewerWidget(style = widgetStyle(true, true))


    }
  }
}

@Preview(device = Devices.TABLET)
@Preview(device = Devices.PHONE)
@Composable
fun HomeScreenPreview(darkTheme: Boolean = true) {
  PreviewDeskflowThemedRoot(darkTheme = darkTheme) { _ -> HomeScreen() }
}

@Preview(device = Devices.TABLET)
@Preview(device = Devices.PHONE)
@Composable
fun HomeScreenPreviewLight() {
  HomeScreenPreview(darkTheme = false)
}
