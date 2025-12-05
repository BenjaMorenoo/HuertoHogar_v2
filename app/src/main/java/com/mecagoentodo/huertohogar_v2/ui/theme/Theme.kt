package com.mecagoentodo.huertohogar_v2.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VerdeEsmeralda,
    secondary = MarronClaro,
    tertiary = AmarilloMostaza,
    background = BlancoSuave,
    surface = BlancoSuave,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = GrisOscuro,
    onSurface = GrisOscuro,
    onSurfaceVariant = GrisMedio
)

@Composable
fun HuertoHogar_v2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Por ahora, forzamos el tema claro para mantener la consistencia con la paleta definida.
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}