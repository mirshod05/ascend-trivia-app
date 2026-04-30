package com.example.ascend_app

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TriviaRepository(context: Context) {

    private val dao  = AppDatabase.getInstance(context).questionDao()
    private val db   = Firebase.firestore
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ascend_prefs", Context.MODE_PRIVATE)

    // ── Public entry point ────────────────────────────────────────────────────

    /**
     * Returns 5 easy + 5 medium + 5 hard questions from the local Room cache.
     * If the cache is empty or Firestore has a newer version, syncs first.
     */
    suspend fun getGameQuestions(): List<Question> {
        syncIfNeeded()
        return buildQuestionSet()
    }

    // ── Sync logic ────────────────────────────────────────────────────────────

    private suspend fun syncIfNeeded() {
        val localVersion  = prefs.getInt("questions_version", 0)
        val remoteVersion = fetchRemoteVersion()

        val cacheEmpty = dao.countByDifficulty("easy") == 0

        if (cacheEmpty || remoteVersion > localVersion) {
            syncFromFirestore()
            prefs.edit().putInt("questions_version", remoteVersion).apply()
        }
    }

    private suspend fun fetchRemoteVersion(): Int {
        return try {
            val doc = db.collection("meta").document("questions_version").get().await()
            doc.getLong("version")?.toInt() ?: 1
        } catch (e: Exception) {
            // Firestore unreachable — use whatever is cached
            prefs.getInt("questions_version", 0)
        }
    }

    private suspend fun syncFromFirestore() {
        val allQuestions = mutableListOf<Question>()

        for (difficulty in listOf("easy", "medium", "hard")) {
            val snapshot = db.collection("questions")
                .whereEqualTo("difficulty", difficulty)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val incorrectList = doc.get("incorrectAnswers") as? List<*>
                        ?: doc.getString("incorrectAnswers")?.split("|")
                        ?: return@mapNotNull null

                    val incorrectStr = incorrectList.filterIsInstance<String>().joinToString("|")

                    Question(
                        question         = doc.getString("question")         ?: return@mapNotNull null,
                        correctAnswer    = doc.getString("correctAnswer")    ?: return@mapNotNull null,
                        incorrectAnswers = incorrectStr,
                        difficulty       = difficulty
                    )
                } catch (e: Exception) { null }
            }
            allQuestions += questions
        }

        if (allQuestions.isNotEmpty()) {
            dao.clearAll()
            dao.insertAll(allQuestions)
        }
    }

    // ── Question set builder ──────────────────────────────────────────────────

    private suspend fun buildQuestionSet(): List<Question> {
        val easy   = dao.getRandom("easy",   5)
        val medium = dao.getRandom("medium", 5)
        val hard   = dao.getRandom("hard",   5)

        val all = easy + medium + hard
        if (all.size < 15) throw Exception(
            "Not enough questions in the local database. Please check your connection and retry."
        )
        return all   // already in easy → medium → hard order
    }
}