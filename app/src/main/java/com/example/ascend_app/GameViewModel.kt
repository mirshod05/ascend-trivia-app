package com.example.ascend_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GameState {
    object Loading : GameState()
    data class Playing(
        val gameManager: GameManager,
        val updateId: Int = 0
    ) : GameState()
    data class GameOver(val winnings: Int) : GameState()
    data class Error(val message: String) : GameState()
}

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TriviaRepository(app.applicationContext)

    private val _state = MutableStateFlow<GameState>(GameState.Loading)
    val gameState: StateFlow<GameState> = _state

    private var gm: GameManager? = null

    init { loadQuestions() }

    fun loadQuestions() {
        _state.value = GameState.Loading
        viewModelScope.launch {
            try {
                val questions = repo.getGameQuestions()
                gm = GameManager(questions)
                _state.value = GameState.Playing(gm!!)
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                val displayMsg = when {
                    msg.contains("Not enough questions") -> msg
                    msg.contains("Firebase") || msg.contains("Firestore") -> "Connection error: $msg"
                    else -> "Error: $msg"
                }
                _state.value = GameState.Error(displayMsg)
            }
        }
    }

    /**
     * Called by the UI AFTER the answer animation finishes.
     * GameManager.answer() is called here — NOT in the UI — so logic runs once.
     */
    fun submitAnswer(answer: String) {
        val manager = gm ?: return
        val current = _state.value as? GameState.Playing ?: return

        val result = manager.answer(answer)

        when {
            result.isCorrect -> {
                if (manager.isGameOver()) {
                    _state.value = GameState.Playing(manager, current.updateId + 1)
                } else {
                    _state.value = GameState.Playing(manager, current.updateId + 1)
                }
            }
            // First wrong in double dip — UI already showed red, now refresh for retry
            result.showDoubleDipOption -> {
                _state.value = GameState.Playing(manager, current.updateId + 1)
            }
            // Wrong with no lifeline — game over
            else -> {
                _state.value = GameState.GameOver(manager.currentWinnings)
            }
        }
    }

    fun useFiftyFifty() { gm?.useFiftyFifty(); refresh() }

    fun usePhoneAFriend(): Boolean {
        val used = gm?.usePhoneAFriend() ?: false
        if (used) refresh()
        return used
    }

    fun useAskAudience(): Boolean {
        val used = gm?.useAskAudience() ?: false
        if (used) refresh()
        return used
    }

    fun useDoubleDip() { gm?.useDoubleDip(); refresh() }

    fun giveUp() {
        _state.value = GameState.GameOver(gm?.giveUp() ?: 0)
    }

    private fun refresh() {
        val current = _state.value as? GameState.Playing ?: return
        _state.value = current.copy(updateId = current.updateId + 1)
    }
}