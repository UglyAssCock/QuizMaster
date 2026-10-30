package com.quizmaster.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quizmaster.app.data.*
import com.quizmaster.app.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

data class ActiveQuizSession(
    val quiz: QuizEntity,
    val questions: List<QuestionItem>,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val correctCount: Int = 0,
    val remainingSeconds: Int = 120,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val namedItemsFound: Set<String> = emptySet(),
    val revealedClueCount: Int = 1,
    val matchedPairs: Set<String> = emptySet(),
    val orderedSelection: List<String> = emptyList(),
    val multiSelectChoices: Set<String> = emptySet(),
    val connectionsSolvedGroups: Set<String> = emptySet(),
    val imageRevealStep: Int = 1,
    val hintTextShown: String? = null,
    val lastAnswerFeedback: String? = null
)

class QuizMasterViewModel(application: Application) : AndroidViewModel(application) {
    private val db = QuizDatabase.getInstance(application)
    private val dao = db.quizDao()
    private val prefs = PreferencesManager(application)
    private val gson = Gson()

    private val _currentScreen = MutableStateFlow(ScreenRoute.HOME)
    val currentScreen: StateFlow<ScreenRoute> = _currentScreen.asStateFlow()

    private val _backStack = mutableListOf<ScreenRoute>()

    val userPrefs: StateFlow<UserPreferencesState> = prefs.prefsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserPreferencesState()
    )

    val allQuizzes: StateFlow<List<QuizEntity>> = dao.getAllQuizzesFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allAttempts: StateFlow<List<QuizAttemptEntity>> = dao.getAllAttemptsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val achievements: StateFlow<List<AchievementEntity>> = dao.getAchievementsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notifications: StateFlow<List<NotificationEntity>> = dao.getNotificationsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val multiplayerRooms: StateFlow<List<MultiplayerRoomEntity>> = dao.getMultiplayerRoomsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedCategory = MutableStateFlow(SeedDataRepository.categories.first())
    val selectedCategory: StateFlow<CategoryGroup> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _selectedGameMode = MutableStateFlow<GameMode?>(null)
    val selectedGameMode: StateFlow<GameMode?> = _selectedGameMode.asStateFlow()

    private val _selectedQuiz = MutableStateFlow<QuizEntity?>(null)
    val selectedQuiz: StateFlow<QuizEntity?> = _selectedQuiz.asStateFlow()

    private val _activeSession = MutableStateFlow<ActiveQuizSession?>(null)
    val activeSession: StateFlow<ActiveQuizSession?> = _activeSession.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterDifficulty = MutableStateFlow("All")
    val filterDifficulty: StateFlow<String> = _filterDifficulty.asStateFlow()

    private val _sortOption = MutableStateFlow("Most Played")
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    private val _levelUpBanner = MutableStateFlow<String?>(null)
    val levelUpBanner: StateFlow<String?> = _levelUpBanner.asStateFlow()

    // Draft state for Quiz Creator & Question Editor
    private val _draftQuestions = MutableStateFlow<List<QuestionItem>>(
        listOf(
            QuestionItem(
                id = "draft_1",
                prompt = "Which country won the 2022 FIFA World Cup?",
                options = listOf("Argentina", "France", "Croatia", "Morocco"),
                correctAnswer = "Argentina",
                acceptableAnswers = listOf("Argentina"),
                explanation = "Argentina won on penalties in Lusail.",
                hint = "Led by Lionel Messi",
                points = 100,
                timeLimitSeconds = 30
            )
        )
    )
    val draftQuestions: StateFlow<List<QuestionItem>> = _draftQuestions.asStateFlow()

    private val _exportedPayloadText = MutableStateFlow("")
    val exportedPayloadText: StateFlow<String> = _exportedPayloadText.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            if (dao.getQuizCount() == 0) {
                dao.insertQuizzes(SeedDataRepository.initialQuizzes())
                dao.insertAchievements(SeedDataRepository.initialAchievements())
                SeedDataRepository.initialNotifications().forEach { dao.insertNotification(it) }
                SeedDataRepository.initialRooms().forEach { dao.upsertMultiplayerRoom(it) }
            }
        }
    }

    fun navigateTo(route: ScreenRoute) {
        if (_currentScreen.value != route) {
            _backStack.add(_currentScreen.value)
            _currentScreen.value = route
        }
    }

    fun navigateBack(): Boolean {
        return if (_backStack.isNotEmpty()) {
            _currentScreen.value = _backStack.removeAt(_backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun selectCategory(category: CategoryGroup, sub: String? = null) {
        _selectedCategory.value = category
        _selectedSubcategory.value = sub
        navigateTo(ScreenRoute.CATEGORY_DETAILS)
    }

    fun selectGameModeFilter(mode: GameMode?) {
        _selectedGameMode.value = mode
        navigateTo(ScreenRoute.EXPLORE)
    }

    fun openQuizDetails(quiz: QuizEntity) {
        _selectedQuiz.value = quiz
        navigateTo(ScreenRoute.QUIZ_DETAILS)
    }

    fun parseQuestions(quiz: QuizEntity): List<QuestionItem> {
        return try {
            val type = object : TypeToken<List<QuestionItem>>() {}.type
            gson.fromJson<List<QuestionItem>>(quiz.questionsJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun startQuiz(quiz: QuizEntity) {
        _selectedQuiz.value = quiz
        val rawQuestions = parseQuestions(quiz)
        val questions = if (quiz.randomizeQuestions) rawQuestions.shuffled() else rawQuestions
        _activeSession.value = ActiveQuizSession(
            quiz = quiz,
            questions = questions,
            currentQuestionIndex = 0,
            score = 0,
            correctCount = 0,
            remainingSeconds = quiz.timeLimitSeconds.coerceAtLeast(30)
        )
        navigateTo(ScreenRoute.QUIZ_GAMEPLAY)
        startTimer()
    }

    fun startRandomQuiz() {
        val list = allQuizzes.value
        if (list.isNotEmpty()) {
            startQuiz(list.random())
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = _activeSession.value ?: break
                if (s.isPaused || s.isFinished) continue
                if (s.remainingSeconds <= 1) {
                    finishActiveQuiz()
                    break
                } else {
                    _activeSession.value = s.copy(remainingSeconds = s.remainingSeconds - 1)
                }
            }
        }
    }

    fun togglePauseSession() {
        val s = _activeSession.value ?: return
        _activeSession.value = s.copy(isPaused = !s.isPaused)
    }

    fun useHint() {
        val s = _activeSession.value ?: return
        val q = s.questions.getOrNull(s.currentQuestionIndex) ?: return
        _activeSession.value = s.copy(
            hintTextShown = q.hint.ifBlank { "Starts with '${q.correctAnswer.take(2)}'" },
            score = (s.score - 15).coerceAtLeast(0)
        )
    }

    fun revealNextClueOrTile() {
        val s = _activeSession.value ?: return
        _activeSession.value = s.copy(
            revealedClueCount = (s.revealedClueCount + 1).coerceAtMost(4),
            imageRevealStep = (s.imageRevealStep + 1).coerceAtMost(4)
        )
    }

    fun skipQuestion() {
        val s = _activeSession.value ?: return
        if (s.currentQuestionIndex + 1 < s.questions.size) {
            _activeSession.value = s.copy(
                currentQuestionIndex = s.currentQuestionIndex + 1,
                hintTextShown = null,
                lastAnswerFeedback = "Skipped",
                revealedClueCount = 1,
                orderedSelection = emptyList(),
                multiSelectChoices = emptySet()
            )
        } else {
            finishActiveQuiz()
        }
    }

    // For Sporcle "Name All" mode
    fun submitNameAllEntry(typedText: String): Boolean {
        val s = _activeSession.value ?: return false
        val clean = typedText.trim().lowercase()
        if (clean.isBlank()) return false
        val matchedQuestion = s.questions.firstOrNull { q ->
            !s.namedItemsFound.contains(q.id) &&
                (q.correctAnswer.lowercase() == clean || q.acceptableAnswers.any { it.lowercase() == clean })
        }
        return if (matchedQuestion != null) {
            SoundEffectHelper.playCorrect(userPrefs.value.soundEnabled)
            val updatedSet = s.namedItemsFound + matchedQuestion.id
            val newScore = s.score + matchedQuestion.points
            _activeSession.value = s.copy(
                namedItemsFound = updatedSet,
                score = newScore,
                correctCount = updatedSet.size,
                lastAnswerFeedback = "Matched: ${matchedQuestion.correctAnswer}!"
            )
            if (updatedSet.size == s.questions.size) {
                finishActiveQuiz()
            }
            true
        } else {
            false
        }
    }

    fun submitAnswer(answer: String) {
        val s = _activeSession.value ?: return
        val q = s.questions.getOrNull(s.currentQuestionIndex) ?: return
        val clean = answer.trim()
        val isCorrect = q.correctAnswer.equals(clean, ignoreCase = true) ||
            q.acceptableAnswers.any { it.equals(clean, ignoreCase = true) }

        if (isCorrect) {
            SoundEffectHelper.playCorrect(userPrefs.value.soundEnabled)
        } else {
            SoundEffectHelper.playWrong(userPrefs.value.soundEnabled)
        }

        val bonusMultiplier = if (s.quiz.gameModeId == GameMode.WHO_AM_I.id) (5 - s.revealedClueCount).coerceAtLeast(1) else 1
        val pointsAdded = if (isCorrect) q.points * bonusMultiplier else 0
        val nextScore = s.score + pointsAdded
        val nextCorrect = s.correctCount + (if (isCorrect) 1 else 0)

        // Endless mode ends immediately on a wrong answer
        if (!isCorrect && s.quiz.gameModeId == GameMode.ENDLESS_MODE.id) {
            _activeSession.value = s.copy(
                score = nextScore,
                correctCount = nextCorrect,
                lastAnswerFeedback = "Game Over! Correct answer was ${q.correctAnswer}"
            )
            finishActiveQuiz()
            return
        }

        if (s.currentQuestionIndex + 1 < s.questions.size) {
            _activeSession.value = s.copy(
                currentQuestionIndex = s.currentQuestionIndex + 1,
                score = nextScore,
                correctCount = nextCorrect,
                hintTextShown = null,
                revealedClueCount = 1,
                imageRevealStep = 1,
                orderedSelection = emptyList(),
                multiSelectChoices = emptySet(),
                lastAnswerFeedback = if (isCorrect) "Correct! +$pointsAdded pts" else "Incorrect (${q.correctAnswer})"
            )
        } else {
            _activeSession.value = s.copy(
                score = nextScore,
                correctCount = nextCorrect
            )
            finishActiveQuiz()
        }
    }

    fun markPairMatched(pairKey: String, totalPairs: Int) {
        val s = _activeSession.value ?: return
        val updated = s.matchedPairs + pairKey
        SoundEffectHelper.playCorrect(userPrefs.value.soundEnabled)
        _activeSession.value = s.copy(
            matchedPairs = updated,
            score = s.score + 100,
            correctCount = updated.size
        )
        if (updated.size >= totalPairs) {
            finishActiveQuiz()
        }
    }

    fun markConnectionsGroupSolved(groupName: String) {
        val s = _activeSession.value ?: return
        val updated = s.connectionsSolvedGroups + groupName
        SoundEffectHelper.playCorrect(userPrefs.value.soundEnabled)
        _activeSession.value = s.copy(
            connectionsSolvedGroups = updated,
            score = s.score + 150,
            correctCount = updated.size
        )
        if (updated.size >= 4) {
            finishActiveQuiz()
        }
    }

    fun finishActiveQuiz() {
        timerJob?.cancel()
        val s = _activeSession.value ?: return
        if (s.isFinished) return

        val totalQ = s.questions.size.coerceAtLeast(1)
        val accuracy = ((s.correctCount.toFloat() / totalQ.toFloat()) * 100).toInt().coerceIn(0, 100)
        val timeTaken = (s.quiz.timeLimitSeconds - s.remainingSeconds).coerceAtLeast(1)
        val perfectBonus = if (accuracy == 100) 100 else 0
        val xpEarned = 50 + (s.correctCount * 20) + perfectBonus

        _activeSession.value = s.copy(isFinished = true)

        viewModelScope.launch {
            val oldRank = UserRank.fromXp(userPrefs.value.xp)
            prefs.addXpAndIncrementStreak(xpEarned, incrementStreak = true)
            val newRank = UserRank.fromXp(userPrefs.value.xp + xpEarned)
            if (newRank.levelNumber > oldRank.levelNumber) {
                SoundEffectHelper.playLevelUp(userPrefs.value.soundEnabled)
                _levelUpBanner.value = "LEVEL UP! You reached Level ${newRank.levelNumber}: ${newRank.title}!"
                dao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Level Up! ${newRank.title}",
                        body = "Congratulations on advancing to Level ${newRank.levelNumber}!",
                        type = "LEVEL_UP"
                    )
                )
            }

            dao.insertAttempt(
                QuizAttemptEntity(
                    attemptId = UUID.randomUUID().toString(),
                    quizId = s.quiz.id,
                    quizTitle = s.quiz.title,
                    category = s.quiz.category,
                    gameModeId = s.quiz.gameModeId,
                    score = s.score,
                    maxScore = totalQ * 100,
                    accuracyPercent = accuracy,
                    timeTakenSeconds = timeTaken,
                    correctCount = s.correctCount,
                    totalQuestions = totalQ,
                    xpEarned = xpEarned
                )
            )

            // Update quiz play count
            dao.insertQuiz(s.quiz.copy(playCount = s.quiz.playCount + 1))

            // Unlock Achievements dynamically
            val currentAch = achievements.value
            currentAch.forEach { ach ->
                var newProg = ach.currentProgress
                when (ach.id) {
                    "ach_first" -> newProg = 1
                    "ach_perfect" -> if (accuracy == 100) newProg = 1
                    "ach_geo_50" -> if (s.quiz.category.contains("Geography", true)) newProg += 1
                    "ach_footy_50" -> if (s.quiz.category.contains("Football", true)) newProg += 1
                    "ach_addict_100" -> newProg += 1
                    "ach_speed" -> if (timeTaken <= 25) newProg = 1
                }
                val unlockedNow = newProg >= ach.targetProgress
                if (newProg != ach.currentProgress || unlockedNow != ach.isUnlocked) {
                    dao.updateAchievement(
                        ach.copy(
                            currentProgress = newProg,
                            isUnlocked = unlockedNow,
                            unlockedAt = if (unlockedNow) System.currentTimeMillis() else ach.unlockedAt
                        )
                    )
                }
            }
        }
        navigateTo(ScreenRoute.QUIZ_RESULTS)
    }

    fun dismissLevelUpBanner() {
        _levelUpBanner.value = null
    }

    fun toggleFavorite(quiz: QuizEntity) {
        viewModelScope.launch {
            val updated = quiz.copy(isFavorite = !quiz.isFavorite)
            dao.insertQuiz(updated)
            if (_selectedQuiz.value?.id == quiz.id) {
                _selectedQuiz.value = updated
            }
        }
    }

    fun toggleOfflineDownload(quiz: QuizEntity) {
        viewModelScope.launch {
            val updated = quiz.copy(isDownloadedOffline = !quiz.isDownloadedOffline)
            dao.insertQuiz(updated)
            if (_selectedQuiz.value?.id == quiz.id) {
                _selectedQuiz.value = updated
            }
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setFilterDifficulty(diff: String) { _filterDifficulty.value = diff }
    fun setSortOption(sort: String) { _sortOption.value = sort }

    // Quiz Creator & Question Workshop
    fun addDraftQuestion(q: QuestionItem) {
        _draftQuestions.value = _draftQuestions.value + q
    }

    fun duplicateDraftQuestion(index: Int) {
        val list = _draftQuestions.value.toMutableList()
        val item = list.getOrNull(index) ?: return
        list.add(index + 1, item.copy(id = UUID.randomUUID().toString(), prompt = "${item.prompt} (Copy)"))
        _draftQuestions.value = list
    }

    fun deleteDraftQuestion(index: Int) {
        val list = _draftQuestions.value.toMutableList()
        if (index in list.indices && list.size > 1) {
            list.removeAt(index)
            _draftQuestions.value = list
        }
    }

    fun moveDraftQuestionUp(index: Int) {
        val list = _draftQuestions.value.toMutableList()
        if (index > 0 && index < list.size) {
            val tmp = list[index - 1]
            list[index - 1] = list[index]
            list[index] = tmp
            _draftQuestions.value = list
        }
    }

    fun publishCustomQuiz(
        title: String,
        description: String,
        category: String,
        subcategory: String,
        difficulty: String,
        gameMode: GameMode,
        timeLimit: Int,
        isPublic: Boolean,
        allowHints: Boolean,
        allowSkipping: Boolean,
        tagsCsv: String
    ) {
        viewModelScope.launch {
            val newQuiz = QuizEntity(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                title = title.ifBlank { "Community Custom Quiz" },
                description = description.ifBlank { "Created in QuizMaster Studio" },
                category = category,
                subcategory = subcategory,
                difficulty = difficulty,
                gameModeId = gameMode.id,
                timeLimitSeconds = timeLimit.coerceAtLeast(30),
                creatorName = userPrefs.value.username,
                isPublic = isPublic,
                isBuiltIn = false,
                isDownloadedOffline = true,
                allowHints = allowHints,
                allowSkipping = allowSkipping,
                tagsCsv = tagsCsv,
                questionsJson = gson.toJson(_draftQuestions.value)
            )
            dao.insertQuiz(newQuiz)
            prefs.addXpAndIncrementStreak(150, incrementStreak = false)
            _selectedQuiz.value = newQuiz
            navigateTo(ScreenRoute.MY_CREATED_QUIZZES)
        }
    }

    fun deleteQuizById(quizId: String) {
        viewModelScope.launch { dao.deleteQuiz(quizId) }
    }

    fun exportDraftQuestionsToCsv(): String {
        val header = "Prompt,CorrectAnswer,Option1,Option2,Option3,Option4,Hint,Points\n"
        val rows = _draftQuestions.value.joinToString("\n") { q ->
            val o1 = q.options.getOrElse(0) { "" }
            val o2 = q.options.getOrElse(1) { "" }
            val o3 = q.options.getOrElse(2) { "" }
            val o4 = q.options.getOrElse(3) { "" }
            "\"${q.prompt}\",\"${q.correctAnswer}\",\"$o1\",\"$o2\",\"$o3\",\"$o4\",\"${q.hint}\",${q.points}"
        }
        val res = header + rows
        _exportedPayloadText.value = res
        return res
    }

    fun exportDraftQuestionsToJson(): String {
        val res = gson.toJson(_draftQuestions.value)
        _exportedPayloadText.value = res
        return res
    }

    fun importQuestionsFromCsv(csvText: String) {
        val lines = csvText.lines().drop(1).filter { it.isNotBlank() }
        val imported = lines.mapIndexedNotNull { idx, line ->
            val cols = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (cols.size >= 2) {
                QuestionItem(
                    id = "imp_${System.currentTimeMillis()}_$idx",
                    prompt = cols[0],
                    correctAnswer = cols[1],
                    options = cols.drop(2).take(4).filter { it.isNotBlank() }.ifEmpty { listOf(cols[1], "Option B", "Option C", "Option D") },
                    hint = cols.getOrElse(6) { "" },
                    points = cols.getOrElse(7) { "100" }.toIntOrNull() ?: 100
                )
            } else null
        }
        if (imported.isNotEmpty()) {
            _draftQuestions.value = _draftQuestions.value + imported
        }
    }

    // Comments & Ratings
    fun getCommentsForQuiz(quizId: String): Flow<List<CommentEntity>> = dao.getCommentsForQuizFlow(quizId)

    fun postComment(quizId: String, text: String, rating: Int) {
        if (text.isBlank()) return
        viewModelScope.launch {
            dao.insertComment(
                CommentEntity(
                    id = UUID.randomUUID().toString(),
                    quizId = quizId,
                    authorName = userPrefs.value.username,
                    text = text.trim(),
                    rating = rating.coerceIn(1, 5)
                )
            )
        }
    }

    fun reportComment(comment: CommentEntity) {
        viewModelScope.launch {
            dao.insertComment(comment.copy(isReported = true))
        }
    }

    // Multiplayer Room Actions
    fun createMultiplayerRoom(quiz: QuizEntity) {
        viewModelScope.launch {
            val code = "QM-${(1000..9999).random()}"
            dao.upsertMultiplayerRoom(
                MultiplayerRoomEntity(
                    roomCode = code,
                    hostName = userPrefs.value.username,
                    guestName = "Waiting for Opponent...",
                    quizId = quiz.id,
                    quizTitle = quiz.title,
                    hostScore = 0,
                    guestScore = 0,
                    status = "WAITING"
                )
            )
        }
    }

    fun joinAndPlayMultiplayerRoom(room: MultiplayerRoomEntity) {
        viewModelScope.launch {
            val simulatedOpponentScore = (300..750).random()
            dao.upsertMultiplayerRoom(
                room.copy(
                    guestName = userPrefs.value.username,
                    guestScore = simulatedOpponentScore,
                    status = "IN_PROGRESS"
                )
            )
            val quiz = allQuizzes.value.firstOrNull { it.id == room.quizId } ?: allQuizzes.value.firstOrNull()
            if (quiz != null) startQuiz(quiz)
        }
    }

    // Daily Deterministic Challenges (Consistent by Date for all users)
    fun getDailyChallenges(): List<QuizEntity> {
        val list = allQuizzes.value
        if (list.isEmpty()) return emptyList()
        val daySeed = LocalDate.now().toEpochDay().toInt()
        val gk = list.filter { it.category.contains("General", true) }.ifEmpty { list }
        val geo = list.filter { it.category.contains("Geography", true) }.ifEmpty { list }
        val sports = list.filter { it.category.contains("Football", true) || it.category.contains("Sports", true) }.ifEmpty { list }
        return listOf(
            gk[Math.abs(daySeed) % gk.size],
            geo[Math.abs(daySeed + 1) % geo.size],
            sports[Math.abs(daySeed + 2) % sports.size],
            list[Math.abs(daySeed + 3) % list.size]
        ).distinctBy { it.id }
    }

    // Settings & Auth actions
    fun updateAccount(username: String, email: String, loggedIn: Boolean) {
        viewModelScope.launch { prefs.updateProfile(username, email, loggedIn) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboarded(true)
            navigateTo(ScreenRoute.HOME)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setTheme(mode) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { prefs.setLanguage(lang) }
    }

    fun setAudioAndNotif(sound: Boolean, music: Boolean, notif: Boolean) {
        viewModelScope.launch { prefs.setAudioAndNotif(sound, music, notif) }
    }

    fun setPremiumStatus(active: Boolean) {
        viewModelScope.launch { prefs.setPremium(active) }
    }

    fun toggleFollowCreator(creator: String) {
        viewModelScope.launch { prefs.toggleFollowCreator(creator) }
    }

    fun markNotificationsRead() {
        viewModelScope.launch { dao.markAllNotificationsRead() }
    }

    fun clearLocalHistoryCache() {
        viewModelScope.launch { dao.clearAllAttempts() }
    }
}
