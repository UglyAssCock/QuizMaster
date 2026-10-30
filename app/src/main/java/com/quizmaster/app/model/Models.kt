package com.quizmaster.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScreenRoute(val title: String) {
    SPLASH("Splash"),
    ONBOARDING("Welcome to QuizMaster"),
    LOGIN("Sign In"),
    REGISTER("Create Account"),
    FORGOT_PASSWORD("Reset Password"),
    HOME("Home Dashboard"),
    EXPLORE("Explore & Discover"),
    CATEGORIES("All Categories"),
    CATEGORY_DETAILS("Category Hub"),
    GAME_MODES("20 Game Modes"),
    QUIZ_DETAILS("Quiz Overview"),
    QUIZ_GAMEPLAY("Live Quiz"),
    QUIZ_RESULTS("Quiz Results"),
    LEADERBOARDS("Global & Category Leaderboards"),
    USER_PROFILE("My Profile"),
    USER_STATISTICS("Deep Analytics & Stats"),
    ACHIEVEMENTS("Achievements & Trophies"),
    DAILY_CHALLENGES("Daily Challenges"),
    FAVORITES("Saved Favorites"),
    RECENTLY_PLAYED("Recently Played"),
    QUIZ_HISTORY("Complete Quiz History"),
    QUIZ_CREATOR("Quiz Studio Creator"),
    QUESTION_EDITOR("Question Workshop"),
    MY_CREATED_QUIZZES("My Created Quizzes"),
    COMMUNITY_QUIZZES("Community Hub"),
    MULTIPLAYER_LOBBY("Head-to-Head Arena"),
    FRIENDS_CHALLENGES("Friends & Direct Challenges"),
    NOTIFICATIONS("Activity & Alerts"),
    SEARCH("Advanced Quiz Search"),
    SETTINGS("Settings & Localization"),
    OFFLINE_DOWNLOADS("Offline Quiz Manager"),
    PREMIUM_SUBSCRIPTION("QuizMaster Pro")
}

enum class GameMode(
    val id: String,
    val displayName: String,
    val description: String,
    val badgeColorHex: Long
) {
    CLASSIC_TRIVIA("classic", "Classic Trivia", "4-option timed multiple choice trivia.", 0xFF4F46E5),
    TYPE_ANSWER("type_answer", "Type the Answer", "Free-form recall without multiple choice hints.", 0xFF0891B2),
    NAME_ALL("name_all", "Name All (Sporcle Grid)", "Recall every item in the list before the clock runs out!", 0xFFEA580C),
    PICTURE_QUIZ("picture", "Picture Quiz", "Identify landmarks, animals, logos and portraits from visual clues.", 0xFF16A34A),
    MAP_QUIZ("map", "Interactive Map Quiz", "Pinpoint countries, capitals and regions on the interactive world canvas.", 0xFF2563EB),
    FLAG_QUIZ("flag", "Flag Quiz", "Identify sovereign flags rendered on the vector flag canvas.", 0xFFDC2626),
    MISSING_WORDS("missing_words", "Missing Words", "Fill in the blank word in famous quotes, titles and facts.", 0xFF9333EA),
    TRUE_FALSE("true_false", "True or False", "Rapid-fire factual verification under pressure.", 0xFF0D9488),
    MULTIPLE_ANSWERS("multi_select", "Multiple Answers", "Select EVERY correct option—partial guesses don't count!", 0xFFD97706),
    MATCHING("matching", "Matching Columns", "Pair items from Column A with their counterparts in Column B.", 0xFF7C3AED),
    ORDERING("ordering", "Ordering", "Arrange items in exact rank order (size, population, distance).", 0xFF0284C7),
    TIMELINE("timeline", "Timeline Chronology", "Place historical milestones in chronological sequence.", 0xFFB45309),
    SPEED_QUIZ("speed", "Speed Blitz", "60-second adrenaline rush—answer as many as humanly possible!", 0xFFE11D48),
    IMAGE_REVEAL("image_reveal", "Image Reveal", "Uncover obscured tiles step-by-step and guess early for bonus points.", 0xFF4338CA),
    CROSSWORD("crossword", "Mini Crossword", "Solve intersecting Across and Down trivia clues.", 0xFF15803D),
    WORD_SCRAMBLE("scramble", "Word Scramble", "Unscramble mixed letters to reveal the hidden term.", 0xFFBE185D),
    WHO_AM_I("who_am_i", "Who Am I?", "Identify the mystery figure using 4 progressive clues (more points for fewer clues!).", 0xFF6D28D9),
    HIGHER_LOWER("higher_lower", "Higher or Lower", "Compare two contenders and guess which metric is higher.", 0xFF047857),
    CONNECTIONS("connections", "Connections 4x4", "Group 16 items into 4 secret thematic categories of 4.", 0xFFB91C1C),
    ENDLESS_MODE("endless", "Endless Survival", "One strike and you're out! How far can your streak go?", 0xFF1E293B)
}

enum class AchievementTier(val label: String, val xpReward: Int, val colorHex: Long) {
    BRONZE("Bronze", 100, 0xFFCD7F32),
    SILVER("Silver", 250, 0xFFC0C0C0),
    GOLD("Gold", 500, 0xFFFFD700),
    PLATINUM("Platinum", 1000, 0xFFE5E4E2)
}

enum class UserRank(val levelNumber: Int, val title: String, val minXp: Int) {
    BEGINNER(1, "Beginner", 0),
    AMATEUR(2, "Amateur", 300),
    APPRENTICE(3, "Apprentice", 800),
    INTERMEDIATE(4, "Intermediate", 1500),
    ADVANCED(5, "Advanced", 2600),
    EXPERT(6, "Expert", 4000),
    MASTER(7, "Master", 6000),
    GRANDMASTER(8, "Grandmaster", 9000),
    GENIUS(9, "Genius", 13000),
    QUIZ_LEGEND(10, "Quiz Legend", 20000);

    companion object {
        fun fromXp(xp: Int): UserRank = entries.lastOrNull { xp >= it.minXp } ?: BEGINNER
        fun nextRank(xp: Int): UserRank? = entries.firstOrNull { it.minXp > xp }
    }
}

data class CategoryGroup(
    val id: String,
    val name: String,
    val colorHex: Long,
    val subcategories: List<String>
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val subcategory: String,
    val difficulty: String, // Easy, Medium, Hard, Master
    val gameModeId: String,
    val timeLimitSeconds: Int,
    val creatorName: String,
    val isPublic: Boolean = true,
    val isBuiltIn: Boolean = true,
    val isDownloadedOffline: Boolean = true,
    val isFavorite: Boolean = false,
    val allowHints: Boolean = true,
    val allowSkipping: Boolean = true,
    val randomizeQuestions: Boolean = false,
    val randomizeAnswers: Boolean = false,
    val passingScorePercent: Int = 60,
    val tagsCsv: String = "",
    val playCount: Int = 0,
    val ratingAverage: Float = 4.8f,
    val ratingCount: Int = 1,
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val questionsJson: String // Serialized List<QuestionItem>
)

data class QuestionItem(
    val id: String,
    val prompt: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val acceptableAnswers: List<String> = emptyList(),
    val explanation: String = "",
    val hint: String = "",
    val points: Int = 100,
    val timeLimitSeconds: Int = 30,
    val visualCode: String = "", // Flag code, Map coordinate "x,y", or illustration key
    val extraPairs: Map<String, String> = emptyMap(), // For Matching, Higher/Lower, Connections
    val clues: List<String> = emptyList() // For Who Am I / Timeline / Ordering
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val attemptId: String,
    val quizId: String,
    val quizTitle: String,
    val category: String,
    val gameModeId: String,
    val score: Int,
    val maxScore: Int,
    val accuracyPercent: Int,
    val timeTakenSeconds: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val tierName: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val authorName: String,
    val text: String,
    val rating: Int,
    val likes: Int = 0,
    val isReported: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: String, // DAILY, STREAK, FRIEND, MULTIPLAYER, ACHIEVEMENT, LEVEL_UP
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "multiplayer_rooms")
data class MultiplayerRoomEntity(
    @PrimaryKey val roomCode: String,
    val hostName: String,
    val guestName: String,
    val quizId: String,
    val quizTitle: String,
    val hostScore: Int,
    val guestScore: Int,
    val status: String, // WAITING, IN_PROGRESS, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)

data class FriendChallengeItem(
    val id: String,
    val friendName: String,
    val avatarSeed: String,
    val statusText: String,
    val targetQuizId: String,
    val targetQuizTitle: String,
    val friendScoreToBeat: Int,
    val isFollowing: Boolean = true
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val xp: Int,
    val bestScore: Int,
    val categoryTag: String,
    val isCurrentUser: Boolean = false
)
