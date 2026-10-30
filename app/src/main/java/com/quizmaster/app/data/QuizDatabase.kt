package com.quizmaster.app.data

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import com.quizmaster.app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "quizmaster_prefs")

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY playCount DESC")
    fun getAllQuizzesFlow(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    suspend fun getQuizById(id: String): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteQuiz(id: String)

    @Query("SELECT COUNT(*) FROM quizzes")
    suspend fun getQuizCount(): Int

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllAttemptsFlow(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM achievements")
    fun getAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(list: List<AchievementEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAchievement(item: AchievementEntity)

    @Query("SELECT * FROM comments WHERE quizId = :quizId AND isReported = 0 ORDER BY timestamp DESC")
    fun getCommentsForQuizFlow(quizId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("SELECT * FROM multiplayer_rooms ORDER BY createdAt DESC")
    fun getMultiplayerRoomsFlow(): Flow<List<MultiplayerRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMultiplayerRoom(room: MultiplayerRoomEntity)

    @Query("DELETE FROM quiz_attempts")
    suspend fun clearAllAttempts()
}

@Database(
    entities = [
        QuizEntity::class,
        QuizAttemptEntity::class,
        AchievementEntity::class,
        CommentEntity::class,
        NotificationEntity::class,
        MultiplayerRoomEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getInstance(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quizmaster_offline.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}

data class UserPreferencesState(
    val username: String = "Alex_Explorer",
    val email: String = "alex@quizmaster.app",
    val isLoggedIn: Boolean = true,
    val hasCompletedOnboarding: Boolean = true,
    val xp: Int = 1450,
    val dailyStreak: Int = 5,
    val weeklyStreak: Int = 2,
    val themeMode: String = "LIGHT", // LIGHT, DARK, SYSTEM
    val languageCode: String = "en", // en, fr, ar
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isPremium: Boolean = false,
    val followedCreatorsCsv: String = "SporcleLegends,GeoAtlas,FootyVault"
)

class PreferencesManager(private val context: Context) {
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
    private val KEY_ONBOARDED = booleanPreferencesKey("onboarded")
    private val KEY_XP = intPreferencesKey("xp")
    private val KEY_STREAK = intPreferencesKey("daily_streak")
    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_LANG = stringPreferencesKey("language_code")
    private val KEY_SOUND = booleanPreferencesKey("sound_enabled")
    private val KEY_MUSIC = booleanPreferencesKey("music_enabled")
    private val KEY_NOTIF = booleanPreferencesKey("notif_enabled")
    private val KEY_PREMIUM = booleanPreferencesKey("is_premium")
    private val KEY_FOLLOWED = stringPreferencesKey("followed_creators")

    val prefsFlow: Flow<UserPreferencesState> = context.dataStore.data.map { pref ->
        UserPreferencesState(
            username = pref[KEY_USERNAME] ?: "Alex_Explorer",
            email = pref[KEY_EMAIL] ?: "alex@quizmaster.app",
            isLoggedIn = pref[KEY_LOGGED_IN] ?: true,
            hasCompletedOnboarding = pref[KEY_ONBOARDED] ?: true,
            xp = pref[KEY_XP] ?: 1450,
            dailyStreak = pref[KEY_STREAK] ?: 5,
            themeMode = pref[KEY_THEME] ?: "LIGHT",
            languageCode = pref[KEY_LANG] ?: "en",
            soundEnabled = pref[KEY_SOUND] ?: true,
            musicEnabled = pref[KEY_MUSIC] ?: true,
            notificationsEnabled = pref[KEY_NOTIF] ?: true,
            isPremium = pref[KEY_PREMIUM] ?: false,
            followedCreatorsCsv = pref[KEY_FOLLOWED] ?: "SporcleLegends,GeoAtlas,FootyVault"
        )
    }

    suspend fun updateProfile(username: String, email: String, loggedIn: Boolean) {
        context.dataStore.edit {
            it[KEY_USERNAME] = username
            it[KEY_EMAIL] = email
            it[KEY_LOGGED_IN] = loggedIn
        }
    }

    suspend fun setOnboarded(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDED] = done }
    }

    suspend fun addXpAndIncrementStreak(addedXp: Int, incrementStreak: Boolean) {
        context.dataStore.edit {
            val currentXp = it[KEY_XP] ?: 1450
            it[KEY_XP] = currentXp + addedXp
            if (incrementStreak) {
                val s = it[KEY_STREAK] ?: 5
                it[KEY_STREAK] = s + 1
            }
        }
    }

    suspend fun setTheme(mode: String) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANG] = lang }
    }

    suspend fun setAudioAndNotif(sound: Boolean, music: Boolean, notif: Boolean) {
        context.dataStore.edit {
            it[KEY_SOUND] = sound
            it[KEY_MUSIC] = music
            it[KEY_NOTIF] = notif
        }
    }

    suspend fun setPremium(premium: Boolean) {
        context.dataStore.edit { it[KEY_PREMIUM] = premium }
    }

    suspend fun toggleFollowCreator(creator: String) {
        context.dataStore.edit {
            val current = (it[KEY_FOLLOWED] ?: "").split(",").filter { s -> s.isNotBlank() }.toMutableSet()
            if (current.contains(creator)) current.remove(creator) else current.add(creator)
            it[KEY_FOLLOWED] = current.joinToString(",")
        }
    }
}

object SoundEffectHelper {
    private var toneGen: ToneGenerator? = null

    private fun generator(): ToneGenerator? {
        return try {
            if (toneGen == null) {
                toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
            }
            toneGen
        } catch (_: Exception) {
            null
        }
    }

    fun playCorrect(enabled: Boolean) {
        if (!enabled) return
        try { generator()?.startTone(ToneGenerator.TONE_PROP_ACK, 160) } catch (_: Exception) {}
    }

    fun playWrong(enabled: Boolean) {
        if (!enabled) return
        try { generator()?.startTone(ToneGenerator.TONE_PROP_NACK, 240) } catch (_: Exception) {}
    }

    fun playLevelUp(enabled: Boolean) {
        if (!enabled) return
        try { generator()?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400) } catch (_: Exception) {}
    }
}
