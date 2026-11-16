package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

val NotoSansBengaliFamily = FontFamily(
    Font(R.font.noto_sans_bengali, FontWeight.Normal),
    Font(R.font.hind_siliguri_bold, FontWeight.Bold)
)

val HindSiliguriDisplayFamily = FontFamily(
    Font(R.font.hind_siliguri_bold, FontWeight.Bold),
    Font(R.font.noto_sans_bengali, FontWeight.Medium)
)

val NotoSerifBengaliFamily = FontFamily(
    Font(R.font.noto_serif_bengali, FontWeight.Normal)
)

val ManobsebaTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = HindSiliguriDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HindSiliguriDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HindSiliguriDisplayFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansBengaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

val ManobsebaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

data class PaletteSpec(
    val key: String,
    val bnName: String,
    val enName: String,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color
)

object ThemePalettes {
    val ALL = listOf(
        PaletteSpec("green", "সবুজ", "Emerald", Color(0xFF087A43), Color(0xFFD6F5E4), Color(0xFF043B20), Color(0xFF0F766E), Color(0xFFCCFBF1)),
        PaletteSpec("blue", "নীল", "Royal Blue", Color(0xFF1D4ED8), Color(0xFFDBEAFE), Color(0xFF0F276B), Color(0xFF0284C7), Color(0xFFE0F2FE)),
        PaletteSpec("violet", "বেগুনি", "Deep Violet", Color(0xFF6D28D9), Color(0xFFEDE9FE), Color(0xFF34106E), Color(0xFF9333EA), Color(0xFFF3E8FF)),
        PaletteSpec("rose", "গোলাপি", "Crimson Rose", Color(0xFFBE123C), Color(0xFFFFE4E6), Color(0xFF5D071D), Color(0xFFE11D48), Color(0xFFFCE7F3)),
        PaletteSpec("orange", "কমলা", "Warm Terracotta", Color(0xFFC2410C), Color(0xFFFFEDD5), Color(0xFF601C04), Color(0xFFD97706), Color(0xFFFEF3C7)),
        PaletteSpec("teal", "টিল", "Coastal Teal", Color(0xFF0F766E), Color(0xFFCCFBF1), Color(0xFF063A36), Color(0xFF0891B2), Color(0xFFCFFAFE)),
        PaletteSpec("maroon", "মেরুন", "Heritage Maroon", Color(0xFF861F3D), Color(0xFFFCE7F0), Color(0xFF450B1C), Color(0xFFB45309), Color(0xFFFEF3C7))
    )

    fun of(key: String): PaletteSpec = ALL.find { it.key == key } ?: ALL.first()
}

private fun buildColorScheme(spec: PaletteSpec, dark: Boolean): ColorScheme {
    return if (dark) {
        darkColorScheme(
            primary = spec.primaryContainer,
            onPrimary = spec.onPrimaryContainer,
            primaryContainer = spec.primary,
            onPrimaryContainer = Color.White,
            secondary = spec.secondaryContainer,
            onSecondary = Color(0xFF0A2220),
            secondaryContainer = spec.secondary,
            onSecondaryContainer = Color.White,
            tertiary = Color(0xFFFBBF24),
            background = Color(0xFF0D1412),
            onBackground = Color(0xFFEDF5F1),
            surface = Color(0xFF131D19),
            onSurface = Color(0xFFEDF5F1),
            surfaceVariant = Color(0xFF1D2B26),
            onSurfaceVariant = Color(0xFFB7C9C1),
            outline = Color(0xFF334740),
            error = Color(0xFFFCA5A5),
            errorContainer = Color(0xFF7F1D1D)
        )
    } else {
        lightColorScheme(
            primary = spec.primary,
            onPrimary = Color.White,
            primaryContainer = spec.primaryContainer,
            onPrimaryContainer = spec.onPrimaryContainer,
            secondary = spec.secondary,
            onSecondary = Color.White,
            secondaryContainer = spec.secondaryContainer,
            onSecondaryContainer = Color(0xFF0B2E2B),
            tertiary = Color(0xFFD97706),
            background = Color(0xFFF4F7F5),
            onBackground = Color(0xFF111C17),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF111C17),
            surfaceVariant = Color(0xFFEAF2EE),
            onSurfaceVariant = Color(0xFF475850),
            outline = Color(0xFFC8D7CF),
            error = Color(0xFFDC2626),
            errorContainer = Color(0xFFFEE2E2)
        )
    }
}

@Composable
fun ManobsebaTheme(
    paletteKey: String = "green",
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val spec = ThemePalettes.of(paletteKey)
    MaterialTheme(
        colorScheme = buildColorScheme(spec, darkTheme),
        typography = ManobsebaTypography,
        shapes = ManobsebaShapes,
        content = content
    )
}
