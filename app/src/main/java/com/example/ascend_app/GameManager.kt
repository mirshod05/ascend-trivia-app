package com.example.ascend_app

object MoneyTiers {
    val tiers = listOf(
        100, 200, 300, 500, 1000,
        2000, 4000, 8000, 16000, 32000,
        64000, 125000, 250000, 500000, 1000000
    )
}

data class LifelineState(
    val fiftyFiftyUsed: Boolean = false,
    val phoneAFriendUsed: Boolean = false,
    val askAudienceUsed: Boolean = false,
    val doubleDipUsed: Boolean = false
)

data class AnswerResult(
    val isCorrect: Boolean,
    val showDoubleDipOption: Boolean = false
)

class GameManager(private val questions: List<Question>) {

    var currentIndex = 0
    var currentWinnings = 0
    var guaranteedWinnings = 0

    var lifelines = LifelineState()

    var eliminated = emptySet<String>()

    var doubleDipActive = false
    private var firstWrongSaved = false

    fun getCurrentQuestion() = questions[currentIndex]

    fun reset() {
        currentIndex = 0
        currentWinnings = 0
        guaranteedWinnings = 0
        lifelines = LifelineState()
        eliminated = emptySet()
        doubleDipActive = false
        firstWrongSaved = false
    }

    fun useFiftyFifty(): Set<String> {
        if (lifelines.fiftyFiftyUsed) return emptySet()

        lifelines = lifelines.copy(fiftyFiftyUsed = true)

        val q = getCurrentQuestion()
        val wrong = q.incorrectAnswersList.shuffled().take(2).toSet()

        eliminated = wrong
        return wrong
    }

    fun usePhoneAFriend(): Boolean {
        if (lifelines.phoneAFriendUsed) return false
        lifelines = lifelines.copy(phoneAFriendUsed = true)
        return true
    }

    fun useAskAudience(): Boolean {
        if (lifelines.askAudienceUsed) return false
        lifelines = lifelines.copy(askAudienceUsed = true)
        return true
    }

    fun useDoubleDip(): Boolean {
        if (lifelines.doubleDipUsed) return false
        lifelines = lifelines.copy(doubleDipUsed = true)
        doubleDipActive = true
        firstWrongSaved = false
        return true
    }

    fun answer(answer: String): AnswerResult {
        val q = getCurrentQuestion()
        val correct = q.correctAnswer == answer

        // DOUBLE DIP MODE
        if (doubleDipActive) {
            if (correct) {
                advance()
                doubleDipActive = false
                return AnswerResult(true)
            }

            // first wrong attempt in double dip
            if (!firstWrongSaved) {
                firstWrongSaved = true
                return AnswerResult(false, showDoubleDipOption = true)
            }

            // second wrong in double dip → game over
            currentWinnings = guaranteedWinnings
            doubleDipActive = false
            return AnswerResult(false)
        }

        // NORMAL MODE
        return if (correct) {
            advance()
            AnswerResult(true)
        } else {
            currentWinnings = guaranteedWinnings
            AnswerResult(false)
        }
    }

    private fun advance() {
        currentWinnings = MoneyTiers.tiers.getOrElse(currentIndex) { currentWinnings }

        if (currentIndex == 4 || currentIndex == 9 || currentIndex == 14) {
            guaranteedWinnings = currentWinnings
        }

        currentIndex++
        eliminated = emptySet()
    }

    fun getCurrentPrize(): Int = MoneyTiers.tiers.getOrElse(currentIndex) { 0 }

    fun giveUp(): Int = guaranteedWinnings

    fun isGameOver(): Boolean = currentIndex >= questions.size
}