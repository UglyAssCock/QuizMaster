package com.quizmaster.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizmaster.app.data.SeedDataRepository
import com.quizmaster.app.model.*
import com.quizmaster.app.ui.*
import com.quizmaster.app.viewmodel.QuizMasterViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QuizMasterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs by viewModel.userPrefs.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val notifications by viewModel.notifications.collectAsState()
            val levelUpBanner by viewModel.levelUpBanner.collectAsState()

            BackHandler {
                if (!viewModel.navigateBack()) {
                    finish()
                }
            }

            QuizMasterTheme(
                themeMode = prefs.themeMode,
                languageCode = prefs.languageCode
            ) {
                Scaffold(
                    topBar = {
                        if (currentScreen != ScreenRoute.SPLASH && currentScreen != ScreenRoute.ONBOARDING) {
                            QuizMasterTopBar(
                                title = currentScreen.title,
                                canNavigateBack = currentScreen != ScreenRoute.HOME,
                                unreadNotifications = notifications.count { !it.isRead },
                                onBack = { viewModel.navigateBack() },
                                onOpenSearch = { viewModel.navigateTo(ScreenRoute.SEARCH) },
                                onOpenNotifications = { viewModel.navigateTo(ScreenRoute.NOTIFICATIONS) },
                                onOpenSettings = { viewModel.navigateTo(ScreenRoute.SETTINGS) }
                            )
                        }
                    },
                    bottomBar = {
                        if (currentScreen != ScreenRoute.SPLASH &&
                            currentScreen != ScreenRoute.ONBOARDING &&
                            currentScreen != ScreenRoute.QUIZ_GAMEPLAY
                        ) {
                            QuizMasterBottomBar(
                                currentScreen = currentScreen,
                                languageCode = prefs.languageCode,
                                onSelectTab = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedVisibility(visible = levelUpBanner != null) {
                            Surface(
                                color = Color(0xFF16A34A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.dismissLevelUpBanner() }
                            ) {
                                Text(
                                    text = levelUpBanner ?: "",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        ScreenRouter(
                            currentScreen = currentScreen,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenRouter(
    currentScreen: ScreenRoute,
    viewModel: QuizMasterViewModel
) {
    val prefs by viewModel.userPrefs.collectAsState()
    val quizzes by viewModel.allQuizzes.collectAsState()
    val attempts by viewModel.allAttempts.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val rooms by viewModel.multiplayerRooms.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedSub by viewModel.selectedSubcategory.collectAsState()
    val selectedMode by viewModel.selectedGameMode.collectAsState()
    val selectedQuiz by viewModel.selectedQuiz.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterDiff by viewModel.filterDifficulty.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val draftQuestions by viewModel.draftQuestions.collectAsState()
    val exportedText by viewModel.exportedPayloadText.collectAsState()
    val context = LocalContext.current

    when (currentScreen) {
        // 1. SPLASH SCREEN
        ScreenRoute.SPLASH -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuizMaster2DLogo(size = 110.dp)
                Spacer(Modifier.height(16.dp))
                Text("QuizMaster", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text("20 Game Modes • Sporcle Grid Engine • Offline-First", fontSize = 13.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { viewModel.navigateTo(ScreenRoute.HOME) }) {
                    Text("Enter QuizMaster")
                }
                TextButton(onClick = { viewModel.navigateTo(ScreenRoute.ONBOARDING) }) {
                    Text("View Onboarding Tour")
                }
            }
        }

        // 2. ONBOARDING SCREEN
        ScreenRoute.ONBOARDING -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    QuizMaster2DLogo(size = 72.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Welcome to QuizMaster", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("• Play 20 interactive quiz modes including Sporcle Name-All Grids, Maps, Flags, Connections & Crosswords.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Build and publish your own quizzes with CSV/JSON import & export right from your Android phone.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Play 100% offline anytime and sync leaderboards & multiplayer rooms with Firebase.")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { viewModel.navigateTo(ScreenRoute.LOGIN) }) {
                        Text("Sign In / Register")
                    }
                    Button(onClick = { viewModel.completeOnboarding() }) {
                        Text("Start Playing")
                    }
                }
            }
        }

        // 3, 4, 5. LOGIN, REGISTRATION, FORGOT PASSWORD SCREENS
        ScreenRoute.LOGIN, ScreenRoute.REGISTER, ScreenRoute.FORGOT_PASSWORD -> {
            var username by remember { mutableStateOf(prefs.username) }
            var email by remember { mutableStateOf(prefs.email) }
            var password by remember { mutableStateOf("") }
            var statusMsg by remember { mutableStateOf<String?>(null) }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text(currentScreen.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (currentScreen == ScreenRoute.REGISTER) {
                        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    if (currentScreen != ScreenRoute.FORGOT_PASSWORD) {
                        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
                    }
                    Button(
                        onClick = {
                            viewModel.updateAccount(username, email, true)
                            statusMsg = if (currentScreen == ScreenRoute.FORGOT_PASSWORD) "Password reset instructions saved locally & dispatched." else "Authenticated as $username"
                            if (currentScreen != ScreenRoute.FORGOT_PASSWORD) viewModel.navigateTo(ScreenRoute.HOME)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(when (currentScreen) {
                            ScreenRoute.LOGIN -> "Sign In"
                            ScreenRoute.REGISTER -> "Create Account"
                            else -> "Send Recovery Link"
                        })
                    }
                    statusMsg?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.LOGIN) }) { Text("Login") }
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.REGISTER) }) { Text("Register") }
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.FORGOT_PASSWORD) }) { Text("Forgot Password?") }
                    }
                }
            }
        }

        // 6. HOME DASHBOARD
        ScreenRoute.HOME -> {
            val rank = UserRank.fromXp(prefs.xp)
            val nextRank = UserRank.nextRank(prefs.xp)
            val dailyList = viewModel.getDailyChallenges()

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                // Welcome & User Level / XP Bar
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Welcome, ${prefs.username}!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                    Text("Level ${rank.levelNumber}: ${rank.title} • ${prefs.xp} XP", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                                    Text(
                                        text = "🔥 ${prefs.dailyStreak}d",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            val progress = if (nextRank != null) {
                                (prefs.xp - rank.minXp).toFloat() / (nextRank.minXp - rank.minXp).coerceAtLeast(1)
                            } else 1f
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.startRandomQuiz() }) {
                                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Random Quiz")
                                }
                                OutlinedButton(onClick = { viewModel.navigateTo(ScreenRoute.DAILY_CHALLENGES) }) {
                                    Text("Daily Challenges (${dailyList.size})")
                                }
                            }
                        }
                    }
                }

                // Quick Navigation Pills for all major Hubs
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val quickHubs = listOf(
                            ScreenRoute.CATEGORIES to "Categories",
                            ScreenRoute.GAME_MODES to "20 Modes",
                            ScreenRoute.LEADERBOARDS to "Leaderboards",
                            ScreenRoute.MULTIPLAYER_LOBBY to "Multiplayer",
                            ScreenRoute.ACHIEVEMENTS to "Achievements",
                            ScreenRoute.COMMUNITY_QUIZZES to "Community",
                            ScreenRoute.OFFLINE_DOWNLOADS to "Offline Pack"
                        )
                        items(quickHubs) { (route, label) ->
                            AssistChip(
                                onClick = { viewModel.navigateTo(route) },
                                label = { Text(label, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }

                // Popular Categories Row
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Explore Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.navigateTo(ScreenRoute.CATEGORIES) }) { Text("See All") }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(SeedDataRepository.categories) { cat ->
                            Surface(
                                color = Color(cat.colorHex),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable { viewModel.selectCategory(cat) }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(cat.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("${cat.subcategories.size} Subtopics", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Continue Playing / Recent Attempts
                if (attempts.isNotEmpty()) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Recently Played", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { viewModel.navigateTo(ScreenRoute.RECENTLY_PLAYED) }) { Text("History") }
                        }
                        val latest = attempts.first()
                        OutlinedCard(modifier = Modifier.fillMaxWidth().clickable {
                            quizzes.firstOrNull { it.id == latest.quizId }?.let { viewModel.openQuizDetails(it) }
                        }) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(latest.quizTitle, fontWeight = FontWeight.Bold)
                                    Text("Last Score: ${latest.score} pts (${latest.accuracyPercent}%)", fontSize = 12.sp)
                                }
                                Text("Replay →", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Featured & Trending Quizzes
                item {
                    Text("Featured & Trending Quizzes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(quizzes.take(8)) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onClick = { viewModel.openQuizDetails(quiz) },
                        onToggleFavorite = { viewModel.toggleFavorite(quiz) }
                    )
                }
            }
        }

        // 7 & 29. EXPLORE DASHBOARD & ADVANCED SEARCH
        ScreenRoute.EXPLORE, ScreenRoute.SEARCH -> {
            val filtered = quizzes.filter { q ->
                val matchesQuery = searchQuery.isBlank() ||
                    q.title.contains(searchQuery, true) ||
                    q.creatorName.contains(searchQuery, true) ||
                    q.category.contains(searchQuery, true) ||
                    q.tagsCsv.contains(searchQuery, true)
                val matchesDiff = filterDiff == "All" || q.difficulty.equals(filterDiff, true)
                val matchesMode = selectedMode == null || q.gameModeId == selectedMode?.id
                matchesQuery && matchesDiff && matchesMode
            }.let { list ->
                when (sortOption) {
                    "Highest Rated" -> list.sortedByDescending { it.ratingAverage }
                    "Newest" -> list.sortedByDescending { it.createdAtEpoch }
                    else -> list.sortedByDescending { it.playCount }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        label = { Text("Search by title, creator, category or tag...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("All", "Easy", "Medium", "Hard")) { diff ->
                            FilterChip(
                                selected = filterDiff == diff,
                                onClick = { viewModel.setFilterDifficulty(diff) },
                                label = { Text(diff) }
                            )
                        }
                        items(listOf("Most Played", "Highest Rated", "Newest")) { sort ->
                            AssistChip(
                                onClick = { viewModel.setSortOption(sort) },
                                label = { Text("Sort: $sort") }
                            )
                        }
                    }
                    if (selectedMode != null) {
                        TextButton(onClick = { viewModel.selectGameModeFilter(null) }) {
                            Text("Clear Mode Filter (${selectedMode?.displayName}) ✕")
                        }
                    }
                }
                items(filtered) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onClick = { viewModel.openQuizDetails(quiz) },
                        onToggleFavorite = { viewModel.toggleFavorite(quiz) }
                    )
                }
            }
        }

        // 8. CATEGORIES SCREEN
        ScreenRoute.CATEGORIES -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(SeedDataRepository.categories) { cat ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.selectCategory(cat) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(cat.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(cat.colorHex))
                            Spacer(Modifier.height(6.dp))
                            Text(cat.subcategories.joinToString(" • "), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 9. CATEGORY DETAILS SCREEN
        ScreenRoute.CATEGORY_DETAILS -> {
            val catQuizzes = quizzes.filter {
                it.category.equals(selectedCategory.name, true) &&
                    (selectedSub == null || it.subcategory.equals(selectedSub, true))
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(selectedCategory.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSub == null,
                                onClick = { viewModel.selectCategory(selectedCategory, null) },
                                label = { Text("All (${selectedCategory.subcategories.size})") }
                            )
                        }
                        items(selectedCategory.subcategories) { sub ->
                            FilterChip(
                                selected = selectedSub == sub,
                                onClick = { viewModel.selectCategory(selectedCategory, sub) },
                                label = { Text(sub) }
                            )
                        }
                    }
                }
                items(catQuizzes.ifEmpty { quizzes }) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onClick = { viewModel.openQuizDetails(quiz) },
                        onToggleFavorite = { viewModel.toggleFavorite(quiz) }
                    )
                }
            }
        }

        // 10. GAME MODES SCREEN (ALL 20 MODES)
        ScreenRoute.GAME_MODES -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("20 Playable Quiz Game Modes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                items(GameMode.entries) { mode ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val matchingQuiz = quizzes.firstOrNull { it.gameModeId == mode.id } ?: quizzes.firstOrNull()
                            if (matchingQuiz != null) viewModel.openQuizDetails(matchingQuiz)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode.displayName, fontWeight = FontWeight.Bold, color = Color(mode.badgeColorHex))
                                Text(mode.description, fontSize = 12.sp)
                            }
                            Button(onClick = {
                                val matchingQuiz = quizzes.firstOrNull { it.gameModeId == mode.id } ?: quizzes.firstOrNull()
                                if (matchingQuiz != null) viewModel.startQuiz(matchingQuiz)
                            }) {
                                Text("Play")
                            }
                        }
                    }
                }
            }
        }

        // 11. QUIZ DETAILS SCREEN
        ScreenRoute.QUIZ_DETAILS -> {
            val quiz = selectedQuiz ?: quizzes.firstOrNull()
            if (quiz != null) {
                val comments by viewModel.getCommentsForQuiz(quiz.id).collectAsState(initial = emptyList())
                var newComment by remember { mutableStateOf("") }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        Text(quiz.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text("${quiz.category} • ${quiz.subcategory} • ${quiz.difficulty}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(quiz.description)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.startQuiz(quiz) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Play Quiz Now")
                            }
                            OutlinedButton(onClick = { viewModel.toggleOfflineDownload(quiz) }) {
                                Text(if (quiz.isDownloadedOffline) "Saved Offline ✓" else "Download")
                            }
                            IconButton(onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Challenge my score on '${quiz.title}' in QuizMaster! https://quizmaster.app/q/${quiz.id}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Quiz"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        Text("Community Reviews & Comments (${comments.size})", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newComment,
                                onValueChange = { newComment = it },
                                label = { Text("Add a comment & 5★ rating...") },
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                viewModel.postComment(quiz.id, newComment, 5)
                                newComment = ""
                            }) {
                                Text("Post")
                            }
                        }
                    }

                    items(comments) { c ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${c.authorName} • ${"★".repeat(c.rating)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(c.text, fontSize = 13.sp)
                                }
                                TextButton(onClick = { viewModel.reportComment(c) }) {
                                    Text("Report", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 12. QUIZ GAMEPLAY SCREEN
        ScreenRoute.QUIZ_GAMEPLAY -> {
            val session = activeSession
            if (session != null) {
                ActiveQuizGameplayScreen(session = session, viewModel = viewModel)
            }
        }

        // 13. QUIZ RESULTS SCREEN
        ScreenRoute.QUIZ_RESULTS -> {
            val latest = attempts.firstOrNull()
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text("Quiz Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                if (latest != null) {
                    Text(latest.quizTitle, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Score: ${latest.score} pts • Accuracy: ${latest.accuracyPercent}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Correct Answers: ${latest.correctCount} / ${latest.totalQuestions} • Time: ${latest.timeTakenSeconds}s")
                    Text("+${latest.xpEarned} XP Earned!", color = Color(0xFF16A34A), fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { selectedQuiz?.let { viewModel.startQuiz(it) } }) {
                        Text("Replay Quiz")
                    }
                    OutlinedButton(onClick = { viewModel.navigateTo(ScreenRoute.LEADERBOARDS) }) {
                        Text("Leaderboards")
                    }
                    OutlinedButton(onClick = { viewModel.navigateTo(ScreenRoute.HOME) }) {
                        Text("Home")
                    }
                }
            }
        }

        // 14. LEADERBOARDS SCREEN
        ScreenRoute.LEADERBOARDS -> {
            var boardTab by remember { mutableStateOf("Global") }
            val entries = listOf(
                LeaderboardEntry(1, "Nadia_Trivia", 21400, 9850, "Global"),
                LeaderboardEntry(2, "Karim_DZ", 18900, 9200, "Geography"),
                LeaderboardEntry(3, prefs.username, prefs.xp, attempts.maxOfOrNull { it.score } ?: 800, "All-Time", isCurrentUser = true),
                LeaderboardEntry(4, "SporcleLegends", 12400, 8100, "Creators"),
                LeaderboardEntry(5, "FootyVault", 9800, 7600, "Football")
            ).sortedByDescending { it.xp }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Global", "Friends", "Weekly", "Monthly", "All-Time", "Category", "Creators")) { tab ->
                            FilterChip(selected = boardTab == tab, onClick = { boardTab = tab }, label = { Text(tab) })
                        }
                    }
                }
                itemsIndexed(entries) { idx, e ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (e.isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("#${idx + 1}  ${e.username}", fontWeight = FontWeight.Bold)
                            Text("${e.xp} XP • Best: ${e.bestScore}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // 15 & 16. USER PROFILE & USER STATISTICS SCREENS
        ScreenRoute.USER_PROFILE, ScreenRoute.USER_STATISTICS -> {
            val rank = UserRank.fromXp(prefs.xp)
            val totalCompleted = attempts.size
            val totalCorrect = attempts.sumOf { it.correctCount }
            val avgAccuracy = if (attempts.isNotEmpty()) attempts.map { it.accuracyPercent }.average().toInt() else 100
            val totalPlaytimeMin = (attempts.sumOf { it.timeTakenSeconds } / 60).coerceAtLeast(1)

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(prefs.username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Text("${prefs.email} • Level ${rank.levelNumber} (${rank.title})", fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Total XP: ${prefs.xp} • Daily Streak: ${prefs.dailyStreak} days")
                        }
                    }
                }
                item {
                    Text("Career Analytics & Personal Records", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Quizzes Played", fontSize = 11.sp)
                                Text("$totalCompleted", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Overall Accuracy", fontSize = 11.sp)
                                Text("$avgAccuracy%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Correct Answers", fontSize = 11.sp)
                                Text("$totalCorrect ($totalPlaytimeMin m)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                item {
                    Text("Profile & Library Shortcuts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val links = listOf(
                        ScreenRoute.USER_STATISTICS to "Detailed Statistics Breakdown",
                        ScreenRoute.ACHIEVEMENTS to "Achievements & Badges (${achievements.count { it.isUnlocked }}/${achievements.size})",
                        ScreenRoute.FAVORITES to "Saved Favorite Quizzes",
                        ScreenRoute.QUIZ_HISTORY to "Complete Quiz Completion History",
                        ScreenRoute.MY_CREATED_QUIZZES to "My Created Quizzes",
                        ScreenRoute.FRIENDS_CHALLENGES to "Friends & Direct Challenges",
                        ScreenRoute.OFFLINE_DOWNLOADS to "Offline Quiz Downloads",
                        ScreenRoute.PREMIUM_SUBSCRIPTION to "QuizMaster Pro Subscription",
                        ScreenRoute.LOGIN to "Switch / Manage Account"
                    )
                    links.forEach { (route, label) ->
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(route) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        // 17. ACHIEVEMENTS SCREEN
        ScreenRoute.ACHIEVEMENTS -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(achievements) { ach ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${ach.title} (${ach.tierName})", fontWeight = FontWeight.Bold)
                                Text(if (ach.isUnlocked) "UNLOCKED ✓" else "${ach.currentProgress}/${ach.targetProgress}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text(ach.description, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (ach.currentProgress.toFloat() / ach.targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 18. DAILY CHALLENGES SCREEN
        ScreenRoute.DAILY_CHALLENGES -> {
            val daily = viewModel.getDailyChallenges()
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Today's 4 Official Daily Challenges", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Synchronized globally by date • Bonus +100 XP on completion!", fontSize = 13.sp)
                }
                items(daily) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onClick = { viewModel.startQuiz(quiz) },
                        onToggleFavorite = { viewModel.toggleFavorite(quiz) }
                    )
                }
            }
        }

        // 19, 20, 21, 24, 25, 31. FAVORITES, RECENTLY PLAYED, HISTORY, MY CREATED, COMMUNITY, OFFLINE DOWNLOADS
        ScreenRoute.FAVORITES,
        ScreenRoute.RECENTLY_PLAYED,
        ScreenRoute.QUIZ_HISTORY,
        ScreenRoute.MY_CREATED_QUIZZES,
        ScreenRoute.COMMUNITY_QUIZZES,
        ScreenRoute.OFFLINE_DOWNLOADS -> {
            val listToShow = when (currentScreen) {
                ScreenRoute.FAVORITES -> quizzes.filter { it.isFavorite }
                ScreenRoute.MY_CREATED_QUIZZES -> quizzes.filter { !it.isBuiltIn || it.creatorName == prefs.username }
                ScreenRoute.OFFLINE_DOWNLOADS -> quizzes.filter { it.isDownloadedOffline }
                else -> quizzes
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(currentScreen.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                if (currentScreen == ScreenRoute.QUIZ_HISTORY || currentScreen == ScreenRoute.RECENTLY_PLAYED) {
                    items(attempts) { att ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(att.quizTitle, fontWeight = FontWeight.Bold)
                                    Text("${att.category} • Score: ${att.score} (${att.accuracyPercent}%)", fontSize = 12.sp)
                                }
                                Text("+${att.xpEarned} XP", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                items(listToShow) { quiz ->
                    QuizCardItem(
                        quiz = quiz,
                        onClick = { viewModel.openQuizDetails(quiz) },
                        onToggleFavorite = { viewModel.toggleFavorite(quiz) }
                    )
                }
            }
        }

        // 22 & 23. QUIZ CREATOR & QUESTION EDITOR
        ScreenRoute.QUIZ_CREATOR, ScreenRoute.QUESTION_EDITOR -> {
            var title by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Geography") }
            var subcategory by remember { mutableStateOf("Capitals") }
            var difficulty by remember { mutableStateOf("Medium") }
            var selectedMode by remember { mutableStateOf(GameMode.CLASSIC_TRIVIA) }
            var timeLimit by remember { mutableStateOf("120") }
            var tags by remember { mutableStateOf("Custom,Trivia") }
            var allowHints by remember { mutableStateOf(true) }
            var allowSkip by remember { mutableStateOf(true) }

            var qPrompt by remember { mutableStateOf("") }
            var qCorrect by remember { mutableStateOf("") }
            var qOptionsCsv by remember { mutableStateOf("Option A,Option B,Option C,Option D") }
            var qHint by remember { mutableStateOf("") }
            var csvImportInput by remember { mutableStateOf("") }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Quiz Studio Creator & Question Workshop", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Quiz Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = subcategory, onValueChange = { subcategory = it }, label = { Text("Subcategory") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = difficulty, onValueChange = { difficulty = it }, label = { Text("Difficulty") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = timeLimit, onValueChange = { timeLimit = it }, label = { Text("Time (sec)") }, modifier = Modifier.weight(1f))
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GameMode.entries) { m ->
                            FilterChip(selected = selectedMode == m, onClick = { selectedMode = m }, label = { Text(m.displayName) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = allowHints, onCheckedChange = { allowHints = it })
                        Text("Allow Hints")
                        Spacer(Modifier.width(12.dp))
                        Checkbox(checked = allowSkip, onCheckedChange = { allowSkip = it })
                        Text("Allow Skipping")
                    }
                }

                // Add Question Box
                item {
                    HorizontalDivider()
                    Text("Add Question to Quiz (${draftQuestions.size} added)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = qPrompt, onValueChange = { qPrompt = it }, label = { Text("Question Prompt / Clue") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qCorrect, onValueChange = { qCorrect = it }, label = { Text("Correct Answer") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qOptionsCsv, onValueChange = { qOptionsCsv = it }, label = { Text("Answer Choices (Comma-separated)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qHint, onValueChange = { qHint = it }, label = { Text("Hint") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (qPrompt.isNotBlank() && qCorrect.isNotBlank()) {
                                viewModel.addDraftQuestion(
                                    QuestionItem(
                                        id = "q_${System.currentTimeMillis()}",
                                        prompt = qPrompt,
                                        options = qOptionsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                        correctAnswer = qCorrect.trim(),
                                        acceptableAnswers = listOf(qCorrect.trim()),
                                        hint = qHint
                                    )
                                )
                                qPrompt = ""
                                qCorrect = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add Question to Draft")
                    }
                }

                itemsIndexed(draftQuestions) { idx, dq ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Q${idx + 1}: ${dq.prompt}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Answer: ${dq.correctAnswer}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Row {
                                IconButton(onClick = { viewModel.moveDraftQuestionUp(idx) }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up") }
                                IconButton(onClick = { viewModel.duplicateDraftQuestion(idx) }) { Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate") }
                                IconButton(onClick = { viewModel.deleteDraftQuestion(idx) }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                            }
                        }
                    }
                }

                // Import / Export CSV & JSON + Publish
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.exportDraftQuestionsToCsv() }, modifier = Modifier.weight(1f)) { Text("Export CSV") }
                        OutlinedButton(onClick = { viewModel.exportDraftQuestionsToJson() }, modifier = Modifier.weight(1f)) { Text("Export JSON") }
                    }
                    if (exportedText.isNotBlank()) {
                        OutlinedTextField(value = exportedText, onValueChange = {}, label = { Text("Exported Data (Copy)") }, modifier = Modifier.fillMaxWidth())
                    }
                    OutlinedTextField(value = csvImportInput, onValueChange = { csvImportInput = it }, label = { Text("Paste CSV rows to Import") }, modifier = Modifier.fillMaxWidth())
                    if (csvImportInput.isNotBlank()) {
                        TextButton(onClick = {
                            viewModel.importQuestionsFromCsv(csvImportInput)
                            csvImportInput = ""
                        }) { Text("Import CSV Questions Now") }
                    }
                    Button(
                        onClick = {
                            viewModel.publishCustomQuiz(
                                title = title,
                                description = desc,
                                category = category,
                                subcategory = subcategory,
                                difficulty = difficulty,
                                gameMode = selectedMode,
                                timeLimit = timeLimit.toIntOrNull() ?: 120,
                                isPublic = true,
                                allowHints = allowHints,
                                allowSkipping = allowSkip,
                                tagsCsv = tags
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Publish Quiz to Community (+150 XP)")
                    }
                }
            }
        }

        // 26 & 27. MULTIPLAYER LOBBY & FRIENDS CHALLENGES
        ScreenRoute.MULTIPLAYER_LOBBY, ScreenRoute.FRIENDS_CHALLENGES -> {
            var joinCode by remember { mutableStateOf("") }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Online Head-to-Head Arena & Friends", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = joinCode, onValueChange = { joinCode = it }, label = { Text("Enter Room Code (e.g. QM-8821)") }, modifier = Modifier.weight(1f))
                        Button(onClick = {
                            val room = rooms.firstOrNull { it.roomCode.equals(joinCode, true) } ?: rooms.firstOrNull()
                            if (room != null) viewModel.joinAndPlayMultiplayerRoom(room)
                        }) { Text("Join") }
                    }
                    Button(
                        onClick = { quizzes.firstOrNull()?.let { viewModel.createMultiplayerRoom(it) } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Create Private Multiplayer Room")
                    }
                }
                items(rooms) { room ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Room ${room.roomCode} • ${room.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${room.quizTitle} (${room.hostName} vs ${room.guestName})", fontSize = 12.sp)
                            }
                            Button(onClick = { viewModel.joinAndPlayMultiplayerRoom(room) }) {
                                Text("Play Match")
                            }
                        }
                    }
                }
            }
        }

        // 28. NOTIFICATIONS SCREEN
        ScreenRoute.NOTIFICATIONS -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { viewModel.markNotificationsRead() }) { Text("Mark All Read") }
                    }
                }
                items(notifications) { n ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(n.title, fontWeight = FontWeight.Bold)
                            Text(n.body, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 30. SETTINGS SCREEN
        ScreenRoute.SETTINGS -> {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Theme Appearance", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LIGHT", "DARK", "SYSTEM").forEach { mode ->
                            FilterChip(selected = prefs.themeMode == mode, onClick = { viewModel.setThemeMode(mode) }, label = { Text(mode) })
                        }
                    }
                }
                item {
                    Text("Language & RTL Layout (English / Français / العربية)", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("en" to "English", "fr" to "Français", "ar" to "العربية (RTL)").forEach { (code, label) ->
                            FilterChip(selected = prefs.languageCode == code, onClick = { viewModel.setLanguage(code) }, label = { Text(label) })
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Sound Effects (SFX)")
                        Switch(checked = prefs.soundEnabled, onCheckedChange = { viewModel.setAudioAndNotif(it, prefs.musicEnabled, prefs.notificationsEnabled) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Background Music")
                        Switch(checked = prefs.musicEnabled, onCheckedChange = { viewModel.setAudioAndNotif(prefs.soundEnabled, it, prefs.notificationsEnabled) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Push Notifications")
                        Switch(checked = prefs.notificationsEnabled, onCheckedChange = { viewModel.setAudioAndNotif(prefs.soundEnabled, prefs.musicEnabled, it) })
                    }
                }
                item {
                    OutlinedButton(onClick = { viewModel.clearLocalHistoryCache() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Clear Local Attempt Cache")
                    }
                    Text("QuizMaster v1.0.0 • Privacy Policy & Terms of Service • Built for Offline & Firebase Cloud Play", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 32. PREMIUM SUBSCRIPTION SCREEN
        ScreenRoute.PREMIUM_SUBSCRIPTION -> {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("QuizMaster Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("• Ad-free experience across all menus (Gameplay is always 100% ad-free for everyone).")
                Text("• Exclusive Pro Badge, Custom Avatar Themes & Deep Category Heatmaps.")
                Text("• Unlimited Offline Encyclopedia Pack synchronization.")
                Button(
                    onClick = { viewModel.setPremiumStatus(!prefs.isPremium) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (prefs.isPremium) "Pro Active ✓ (Tap to Manage)" else "Activate QuizMaster Pro")
                }
            }
        }
    }
}
