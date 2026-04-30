package com.example.ascend_app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    // Pull a random sample of N questions at a given difficulty
    @Query("SELECT * FROM questions WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandom(difficulty: String, count: Int): List<Question>

    @Query("SELECT COUNT(*) FROM questions WHERE difficulty = :difficulty")
    suspend fun countByDifficulty(difficulty: String): Int

    @Query("DELETE FROM questions")
    suspend fun clearAll()
}