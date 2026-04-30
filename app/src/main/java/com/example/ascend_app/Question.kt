package com.example.ascend_app

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// ── API model (from Open Trivia DB) ──────────────────────────────────────────
// Used only during the Firestore sync. Not stored in Room directly.

data class ApiQuestion(
    val question: String,
    @SerializedName("correct_answer")   val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>,
    val difficulty: String
)

// ── Room entity (local DB) ────────────────────────────────────────────────────
// Stored flat — incorrect answers joined with | so Room doesn't need a
// separate table. Decoded at read time.

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,           // plain text (already decoded before storing)
    val correctAnswer: String,      // plain text
    val incorrectAnswers: String,   // pipe-separated: "Wrong 1|Wrong 2|Wrong 3"
    val difficulty: String          // "easy" | "medium" | "hard"
) {
    val incorrectAnswersList: List<String>
        get() = incorrectAnswers.split("|")

    val allAnswersShuffled: List<String>
        get() = (incorrectAnswersList + correctAnswer).shuffled()
}

// ── Conversion helper ─────────────────────────────────────────────────────────

fun ApiQuestion.toQuestion(): Question {
    fun String.decodeB64(): String =
        String(Base64.decode(this, Base64.DEFAULT)).trim()

    return Question(
        question         = question.decodeB64(),
        correctAnswer    = correctAnswer.decodeB64(),
        incorrectAnswers = incorrectAnswers.joinToString("|") { it.decodeB64() },
        difficulty       = difficulty
    )
}