package ru.slovodel.scorekeeper.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.slovodel.scorekeeper.game.GameEngine
import ru.slovodel.scorekeeper.model.GameState
import ru.slovodel.scorekeeper.model.LetterBonus
import ru.slovodel.scorekeeper.storage.GameRepository

class GameController(context: Context) {
    private val repository = GameRepository(context)

    var state by mutableStateOf(repository.load())
        private set

    fun addPlayer(name: String) = update { GameEngine.addPlayer(it, name) }

    fun removePlayer(index: Int) = update { GameEngine.removePlayer(it, index) }

    fun startGame() = update { GameEngine.startGame(it) }

    fun recordWord(word: String, letterBonuses: List<LetterBonus>, wordMultiplier: Int) =
        update { GameEngine.recordWord(it, word, letterBonuses, wordMultiplier) }

    fun skipTurn() = update { GameEngine.skipTurn(it) }

    fun undoLastTurn() = update { GameEngine.undoLastTurn(it) }

    fun finishGame() = update { GameEngine.finishGame(it) }

    fun resetGame() {
        state = GameEngine.resetGame()
        repository.clear()
    }

    private fun update(transform: (GameState) -> GameState) {
        state = transform(state)
        repository.save(state)
    }
}
