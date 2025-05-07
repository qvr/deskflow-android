package org.tfv.deskflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Immutable
data class DeskflowExtendedColorScheme(
  val toolbar: Color,
  val onToolbar: Color,
  val toolbarItemChecked: Color,
  val onToolbarItemChecked: Color,
  val toolbarItemFocused: Color,
  val onToolbarItemFocused: Color,
  val warning: Color,
  val onWarning: Color,
  val success: Color,
  val onSuccess: Color,
  val keyboardBackground: Color,
  val onKeyboardBackground: Color,
)

val LocalDeskflowExtendedColorScheme =
  staticCompositionLocalOf<DeskflowExtendedColorScheme> {
    error("No DeskflowExtendedColorScheme provided")
  }

private val baseLightScheme =
  lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    //  background = backgroundLight,
    //  onBackground = onBackgroundLight,
    background = primaryContainerLight,
    onBackground = onPrimaryContainerLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
  )

private val baseDarkScheme =
  darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    //  background = backgroundDark,
    //  onBackground = onBackgroundDark,
    background = primaryContainerDark,
    onBackground = onPrimaryContainerDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
  )

val deskflowDarkColorScheme =
  DeskflowExtendedColorScheme(
    toolbar = toolbarDark,
    onToolbar = onToolbarDark,
    toolbarItemChecked = toolbarItemCheckedDark,
    onToolbarItemChecked = onToolbarItemCheckedDark,
    toolbarItemFocused = toolbarItemFocusedDark,
    onToolbarItemFocused = onToolbarItemFocusedDark,
    warning = warningDark,
    onWarning = onWarningDark,
    success = successDark,
    onSuccess = onSuccessDark,
    keyboardBackground = keyboardBackgroundDark,
    onKeyboardBackground = onKeyboardBackgroundDark
  )

val deskflowLightColorScheme =
  DeskflowExtendedColorScheme(
    toolbar = toolbarLight,
    onToolbar = onToolbarLight,
    toolbarItemChecked = toolbarItemCheckedLight,
    onToolbarItemChecked = onToolbarItemCheckedLight,
    toolbarItemFocused = toolbarItemFocusedLight,
    onToolbarItemFocused = onToolbarItemFocusedLight,
    warning = warningDark,
    onWarning = onWarningDark,
    success = successDark,
    onSuccess = onSuccessDark,
    keyboardBackground = keyboardBackgroundDark,
    onKeyboardBackground = onKeyboardBackgroundDark
  )

/** Light Android gradient colors */
val LightAndroidGradientColors = GradientColors(container = primaryLight)

/** Dark Android gradient colors */
val DarkAndroidGradientColors = GradientColors(container = primaryDark)

/** Light Android background theme */
val LightAndroidBackgroundTheme = BackgroundTheme(color = primaryLight)

/** Dark Android background theme */
val DarkAndroidBackgroundTheme = BackgroundTheme(color = primaryDark)

/**
 * Now in Android theme.
 *
 * @param darkTheme Whether the theme should use a dark color scheme (follows
 *   system by default).
 * @param androidTheme Whether the theme should use the Android theme color
 *   scheme instead of the default theme.
 * @param disableDynamicTheming If `true`, disables the use of dynamic theming,
 *   even when it is supported. This parameter has no effect if [androidTheme]
 *   is `true`.
 */
@Composable
fun DeskflowTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  disableDynamicTheming: Boolean = true,
  content: @Composable () -> Unit,
) {
  // Color scheme
  val (colorScheme, extColorScheme) =
    when {
      !disableDynamicTheming -> {
        val context = LocalContext.current
        if (darkTheme)
          Pair(dynamicDarkColorScheme(context), deskflowDarkColorScheme)
        else Pair(dynamicLightColorScheme(context), deskflowLightColorScheme)
      }
      darkTheme -> Pair(baseDarkScheme, deskflowDarkColorScheme)

      else -> Pair(baseLightScheme, deskflowLightColorScheme)
    }
  // Gradient colors
  val emptyGradientColors =
    GradientColors(container = colorScheme.surfaceColorAtElevation(2.dp))
  val defaultGradientColors =
    GradientColors(
      top = colorScheme.inverseOnSurface,
      bottom = colorScheme.primaryContainer,
      container = colorScheme.surface,
    )
  val gradientColors =
    when {
      !disableDynamicTheming -> emptyGradientColors
      else -> defaultGradientColors
    }
  // Background theme
  val defaultBackgroundTheme =
    BackgroundTheme(color = colorScheme.surface, tonalElevation = 2.dp)
  val backgroundTheme = defaultBackgroundTheme

  val tintTheme =
    when {
      !disableDynamicTheming -> TintTheme(colorScheme.primary)
      else -> TintTheme()
    }

  // Composition locals
  CompositionLocalProvider(
    LocalDeskflowExtendedColorScheme provides extColorScheme,
    LocalGradientColors provides gradientColors,
    LocalBackgroundTheme provides backgroundTheme,
    LocalTintTheme provides tintTheme,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = DeskflowTypography,
      content = content,
    )
  }
}
