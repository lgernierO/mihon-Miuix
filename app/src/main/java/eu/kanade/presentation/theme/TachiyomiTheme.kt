package eu.kanade.presentation.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import eu.kanade.domain.ui.model.AppTheme
import eu.kanade.presentation.theme.colorscheme.BaseColorScheme
import eu.kanade.presentation.theme.colorscheme.CatppuccinColorScheme
import eu.kanade.presentation.theme.colorscheme.GreenAppleColorScheme
import eu.kanade.presentation.theme.colorscheme.LavenderColorScheme
import eu.kanade.presentation.theme.colorscheme.MidnightDuskColorScheme
import eu.kanade.presentation.theme.colorscheme.MonetColorScheme
import eu.kanade.presentation.theme.colorscheme.MonochromeColorScheme
import eu.kanade.presentation.theme.colorscheme.NordColorScheme
import eu.kanade.presentation.theme.colorscheme.StrawberryColorScheme
import eu.kanade.presentation.theme.colorscheme.TachiyomiColorScheme
import eu.kanade.presentation.theme.colorscheme.TakoColorScheme
import eu.kanade.presentation.theme.colorscheme.TealTurqoiseColorScheme
import eu.kanade.presentation.theme.colorscheme.TidalWaveColorScheme
import eu.kanade.presentation.theme.colorscheme.TokyoNightColorScheme
import eu.kanade.presentation.theme.colorscheme.YinYangColorScheme
import eu.kanade.presentation.theme.colorscheme.YotsubaColorScheme
import mihon.app.di.appGraph
import tachiyomi.presentation.core.theme.containerForSelectedIndicator
import tachiyomi.presentation.core.theme.contentColorWithContrast
import tachiyomi.presentation.core.theme.neutralSelectionContainer
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.TextStyles
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.defaultTextStyles
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Composable
fun TachiyomiTheme(
    appTheme: AppTheme? = null,
    amoled: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val uiPreferences = remember { context.appGraph.uiPreferences }
    BaseTachiyomiTheme(
        appTheme = appTheme ?: uiPreferences.appTheme.get(),
        isAmoled = amoled ?: uiPreferences.themeDarkAmoled.get(),
        content = content,
    )
}

@Composable
fun TachiyomiPreviewTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit,
) = BaseTachiyomiTheme(appTheme, isAmoled, content)

@Composable
private fun BaseTachiyomiTheme(
    appTheme: AppTheme,
    isAmoled: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val legacyScheme = remember(context, appTheme, isDark, isAmoled) {
        getThemeColorScheme(context, appTheme, isDark, isAmoled)
    }
    val controller = remember(context, appTheme, isDark, isAmoled) {
        ThemeController(
            colorSchemeMode = if (appTheme == AppTheme.MONET && !isAmoled) {
                ColorSchemeMode.MonetSystem
            } else {
                ColorSchemeMode.System
            },
            lightColors = getThemeColorScheme(context, appTheme, false, false)
                .toMiuixColors(
                    dark = false,
                    stock = appTheme == AppTheme.DEFAULT,
                    adaptNeutralSelection = appTheme != AppTheme.MONET,
                ),
            darkColors = getThemeColorScheme(context, appTheme, true, isAmoled)
                .toMiuixColors(
                    dark = true,
                    stock = appTheme == AppTheme.DEFAULT,
                    amoled = isAmoled,
                    adaptNeutralSelection = appTheme != AppTheme.MONET,
                ),
            isDark = isDark,
        )
    }

    val colors = controller.currentColors()
    val textStyles = remember { defaultTextStyles() }
    val materialColors = remember(colors, legacyScheme, isDark, isAmoled) {
        colors.toMaterialColors(legacyScheme, isDark && isAmoled)
    }
    // Third-party renderers and the Android View reader consume Material locals.
    // Miuix is innermost so its indication and overscroll also own native controls.
    MaterialTheme(
        colorScheme = materialColors,
        typography = textStyles.toMaterialTypography(),
        shapes = Shapes(),
    ) {
        MiuixTheme(controller = controller, textStyles = textStyles) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides colors.onSurface,
                content = content,
            )
        }
    }
}

private fun Colors.withReadableContentColors(): Colors = copy(
    onPrimary = primary.contentColorWithContrast(onPrimary, onSurface),
    onPrimaryVariant = primaryVariant.contentColorWithContrast(onPrimaryVariant, onSurface),
    onPrimaryContainer = primaryContainer.contentColorWithContrast(onPrimaryContainer, onSurface),
    onSecondaryVariant = secondaryVariant.contentColorWithContrast(onSecondaryVariant, onSurface),
    onSecondaryContainer = secondaryContainer.contentColorWithContrast(onSecondaryContainer, onSurface),
    onTertiaryContainer = tertiaryContainer.contentColorWithContrast(onTertiaryContainer, onSurface),
)

private fun Colors.withDistinctNeutralSelection(): Colors {
    val cardSurface = surfaceContainer.containerForSelectedIndicator(primary, surface)
    val selectedSurface = secondaryVariant.neutralSelectionContainer(
        onSurface = onSurface,
        backgrounds = listOf(background, surface, cardSurface),
    )
    return copy(
        surfaceContainer = cardSurface,
        secondaryVariant = selectedSurface,
        onSecondaryVariant = selectedSurface.contentColorWithContrast(onSecondaryVariant, onSurface),
    )
}

private fun Colors.withDisabledSecondaryColors(): Colors {
    val disabledContainer = secondaryVariant.copy(alpha = 0.6f).compositeOver(surface)
    return copy(
        disabledSecondaryVariant = disabledContainer,
        disabledOnSecondaryVariant = onSurface.copy(alpha = 0.38f).compositeOver(disabledContainer),
    )
}

private fun ColorScheme.toMiuixColors(
    dark: Boolean,
    stock: Boolean,
    amoled: Boolean = false,
    adaptNeutralSelection: Boolean = true,
): Colors {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    val colors = if (stock) {
        base
    } else {
        base.copy(
            primary = primary,
            onPrimary = onPrimary,
            primaryVariant = primary,
            onPrimaryVariant = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            disabledPrimary = primary.copy(alpha = 0.38f).compositeOver(surface),
            disabledOnPrimary = onPrimary.copy(alpha = 0.38f)
                .compositeOver(primary.copy(alpha = 0.38f).compositeOver(surface)),
            disabledPrimaryButton = primary.copy(alpha = 0.18f).compositeOver(surface),
            disabledOnPrimaryButton = onSurface.copy(alpha = 0.38f).compositeOver(surface),
            disabledPrimarySlider = primary.copy(alpha = 0.18f).compositeOver(surface),
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            surfaceContainer = surfaceContainer,
            onSurfaceContainer = onSurface,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            onSurfaceContainerHighest = onSurface,
            onSurfaceVariantSummary = onSurfaceVariant,
            onSurfaceVariantActions = onSurfaceVariant,
            secondary = outlineVariant,
            onSecondary = outline,
            disabledSecondary = outlineVariant.copy(alpha = 0.5f).compositeOver(surface),
            disabledOnSecondary = onSurface.copy(alpha = 0.38f)
                .compositeOver(outlineVariant.copy(alpha = 0.5f).compositeOver(surface)),
            secondaryVariant = surfaceContainerHigh,
            onSecondaryVariant = onSurface,
            secondaryContainer = surfaceContainerHigh,
            onSecondaryContainer = onSurface,
            secondaryContainerVariant = surfaceContainerHighest,
            onSecondaryContainerVariant = onSurfaceVariant,
            tertiaryContainer = primary.copy(alpha = 0.12f).compositeOver(surfaceContainer),
            onTertiaryContainer = primary.copy(alpha = 0.12f).compositeOver(surfaceContainer)
                .contentColorWithContrast(primary, onSurface),
            onBackgroundVariant = primary,
            onSurfaceSecondary = onSurface.copy(alpha = 0.8f).compositeOver(surface),
            disabledOnSurface = onSurface.copy(alpha = 0.38f).compositeOver(surface),
            onSurfaceContainerVariant = onSurfaceVariant,
            onSurfaceContainerHigh = onSurface,
            sliderKeyPoint = primary,
            sliderKeyPointForeground = surfaceContainerHigh,
            sliderBackground = primary.copy(alpha = 0.2f).compositeOver(surfaceContainer),
            outline = outline,
            dividerLine = outlineVariant,
        )
    }
    val resolvedColors = if (dark && amoled) {
        colors.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainer = Color(0xFF0C0C0C),
            surfaceContainerHigh = Color(0xFF131313),
            surfaceContainerHighest = Color(0xFF1B1B1B),
        )
    } else {
        colors
    }
    return if (stock) {
        resolvedColors
    } else {
        val selectionColors = if (adaptNeutralSelection) {
            resolvedColors.withDistinctNeutralSelection()
        } else {
            resolvedColors
        }
        selectionColors.withReadableContentColors().withDisabledSecondaryColors()
    }
}

private fun Colors.toMaterialColors(legacy: ColorScheme, amoled: Boolean): ColorScheme = legacy.copy(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = secondaryVariant,
    onSecondaryContainer = onSecondaryVariant,
    background = if (amoled) Color.Black else surface,
    onBackground = onSurface,
    surface = if (amoled) Color.Black else surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariantSummary,
    surfaceContainerLowest = background,
    surfaceContainerLow = surfaceContainer,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
    outline = outline,
    outlineVariant = dividerLine,
    surfaceTint = primary,
    inverseSurface = onSecondaryVariant,
    inverseOnSurface = secondaryVariant,
    inversePrimary = onPrimaryVariant,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
)

private fun TextStyles.toMaterialTypography(): Typography = Typography(
    displayLarge = title1,
    displayMedium = title1,
    displaySmall = title2,
    headlineLarge = title1,
    headlineMedium = title2,
    headlineSmall = title3,
    titleLarge = title3,
    titleMedium = headline1,
    titleSmall = headline2,
    bodyLarge = main,
    bodyMedium = body1,
    bodySmall = body2,
    labelLarge = button,
    labelMedium = footnote1,
    labelSmall = footnote2,
)

private fun getThemeColorScheme(
    context: Context,
    appTheme: AppTheme,
    isDark: Boolean,
    isAmoled: Boolean,
): ColorScheme {
    val colorScheme = if (appTheme == AppTheme.MONET) {
        MonetColorScheme(context)
    } else {
        colorSchemes.getOrDefault(appTheme, TachiyomiColorScheme)
    }
    return colorScheme.getColorScheme(
        isDark = isDark,
        isAmoled = isAmoled,
        overrideDarkSurfaceContainers = appTheme != AppTheme.MONET,
    )
}

private val colorSchemes: Map<AppTheme, BaseColorScheme> = mapOf(
    AppTheme.DEFAULT to TachiyomiColorScheme,
    AppTheme.CATPPUCCIN to CatppuccinColorScheme,
    AppTheme.TOKYONIGHT to TokyoNightColorScheme,
    AppTheme.GREEN_APPLE to GreenAppleColorScheme,
    AppTheme.LAVENDER to LavenderColorScheme,
    AppTheme.MIDNIGHT_DUSK to MidnightDuskColorScheme,
    AppTheme.MONOCHROME to MonochromeColorScheme,
    AppTheme.NORD to NordColorScheme,
    AppTheme.STRAWBERRY_DAIQUIRI to StrawberryColorScheme,
    AppTheme.TAKO to TakoColorScheme,
    AppTheme.TEALTURQUOISE to TealTurqoiseColorScheme,
    AppTheme.TIDAL_WAVE to TidalWaveColorScheme,
    AppTheme.YINYANG to YinYangColorScheme,
    AppTheme.YOTSUBA to YotsubaColorScheme,
)
