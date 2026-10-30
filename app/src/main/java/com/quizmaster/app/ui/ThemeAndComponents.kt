package com.quizmaster.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizmaster.app.model.GameMode
import com.quizmaster.app.model.QuizEntity
import com.quizmaster.app.model.ScreenRoute

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    tertiary = Color(0xFFEA580C),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF083344),
    tertiary = Color(0xFFFB923C),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun QuizMasterTheme(
    themeMode: String,
    languageCode: String,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val layoutDir = if (languageCode == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        MaterialTheme(
            colorScheme = if (useDark) DarkColors else LightColors,
            content = content
        )
    }
}

/**
 * Strictly 2D flat geometric vector logo with zero drop shadows.
 */
@Composable
fun QuizMaster2DLogo(size: Dp = 64.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Flat 2D Hexagon badge
        val hexPath = Path().apply {
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.75f)
            lineTo(0f, h * 0.25f)
            close()
        }
        drawPath(hexPath, color = Color(0xFF4F46E5))

        // Flat 2D Inner Ring ("Q")
        drawCircle(
            color = Color(0xFFFBBF24),
            radius = w * 0.26f,
            center = Offset(w * 0.5f, h * 0.46f),
            style = Stroke(width = w * 0.1f)
        )
        // Flat Q tail lightning bolt
        drawLine(
            color = Color(0xFF22D3EE),
            start = Offset(w * 0.58f, h * 0.56f),
            end = Offset(w * 0.78f, h * 0.78f),
            strokeWidth = w * 0.1f
        )
    }
}

@Composable
fun FlagCanvasView(flagCode: String, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE2E8F0))
    ) {
        val w = size.width
        val h = size.height
        when (flagCode) {
            "FLAG_FR" -> {
                drawRect(Color(0xFF002395), Offset(0f, 0f), Size(w / 3f, h))
                drawRect(Color.White, Offset(w / 3f, 0f), Size(w / 3f, h))
                drawRect(Color(0xFFED2939), Offset(2f * w / 3f, 0f), Size(w / 3f, h))
            }
            "FLAG_JP" -> {
                drawRect(Color.White, Offset(0f, 0f), Size(w, h))
                drawCircle(Color(0xFFBC002D), radius = h * 0.3f, center = Offset(w / 2f, h / 2f))
            }
            "FLAG_IT" -> {
                drawRect(Color(0xFF009246), Offset(0f, 0f), Size(w / 3f, h))
                drawRect(Color.White, Offset(w / 3f, 0f), Size(w / 3f, h))
                drawRect(Color(0xFFCE2B37), Offset(2f * w / 3f, 0f), Size(w / 3f, h))
            }
            else -> {
                // Germany Horizontal Tricolor
                drawRect(Color(0xFF000000), Offset(0f, 0f), Size(w, h / 3f))
                drawRect(Color(0xFFDD0000), Offset(0f, h / 3f), Size(w, h / 3f))
                drawRect(Color(0xFFFFCE00), Offset(0f, 2f * h / 3f), Size(w, h / 3f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizMasterTopBar(
    title: String,
    canNavigateBack: Boolean,
    unreadNotifications: Int,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuizMaster2DLogo(size = 28.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            BadgedBox(
                badge = {
                    if (unreadNotifications > 0) {
                        Badge { Text("$unreadNotifications") }
                    }
                }
            ) {
                IconButton(onClick = onOpenNotifications) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
fun QuizMasterBottomBar(
    currentScreen: ScreenRoute,
    languageCode: String,
    onSelectTab: (ScreenRoute) -> Unit
) {
    val tabs = listOf(
        Triple(ScreenRoute.HOME, Icons.Default.Home, when (languageCode) { "fr" -> "Accueil"; "ar" -> "الرئيسية"; else -> "Home" }),
        Triple(ScreenRoute.EXPLORE, Icons.Default.Explore, when (languageCode) { "fr" -> "Explorer"; "ar" -> "استكشاف"; else -> "Explore" }),
        Triple(ScreenRoute.GAME_MODES, Icons.Default.SportsEsports, when (languageCode) { "fr" -> "Jouer"; "ar" -> "العب"; else -> "Play" }),
        Triple(ScreenRoute.QUIZ_CREATOR, Icons.Default.AddCircle, when (languageCode) { "fr" -> "Créer"; "ar" -> "إنشاء"; else -> "Create" }),
        Triple(ScreenRoute.USER_PROFILE, Icons.Default.Person, when (languageCode) { "fr" -> "Profil"; "ar" -> "حسابي"; else -> "Profile" })
    )
    NavigationBar {
        tabs.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentScreen == route,
                onClick = { onSelectTab(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun QuizCardItem(
    quiz: QuizEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val mode = GameMode.entries.firstOrNull { it.id == quiz.gameModeId } ?: GameMode.CLASSIC_TRIVIA
    val questionsCount = quiz.questionsJson.split("\"id\"").size - 1

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(mode.badgeColorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${quiz.category} • ${mode.displayName}",
                        color = Color(mode.badgeColorHex),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (quiz.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = if (quiz.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Favorite"
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = quiz.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${quiz.creatorName} • ${questionsCount.coerceAtLeast(1)} Qs • ${quiz.difficulty}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${quiz.ratingAverage} (${quiz.playCount} plays)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
            }
        }
    }
}
