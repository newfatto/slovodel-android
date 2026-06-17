package ru.slovodel.scorekeeper.game

import java.util.Locale
import ru.slovodel.scorekeeper.model.GamePhase
import ru.slovodel.scorekeeper.model.GameState
import ru.slovodel.scorekeeper.model.HistoryEntry
import ru.slovodel.scorekeeper.model.LetterBonus
import ru.slovodel.scorekeeper.model.Player
import ru.slovodel.scorekeeper.model.RankedPlayer

object GameEngine {
    fun addPlayer(state: GameState, rawName: String): GameState {
        val name = rawName.trim()
        require(name.isNotEmpty()) { "Введите имя игрока." }
        val normalized = name.lowercase(Locale("ru"))
        require(state.players.none { it.name.lowercase(Locale("ru")) == normalized }) {
            "Игрок с таким именем уже добавлен."
        }
        require(state.phase == GamePhase.SETUP) { "Игроков можно менять только до начала игры." }
        return state.copy(players = state.players + Player(name))
    }

    fun removePlayer(state: GameState, index: Int): GameState {
        require(state.phase == GamePhase.SETUP) { "Игроков можно менять только до начала игры." }
        return state.copy(players = state.players.filterIndexed { i, _ -> i != index })
    }

    fun startGame(state: GameState): GameState {
        require(state.players.isNotEmpty()) { "Добавьте хотя бы одного игрока." }
        return state.copy(phase = GamePhase.PLAYING, currentPlayerIndex = 0, round = 1)
    }

    fun recordWord(
        state: GameState,
        word: String,
        letterBonuses: List<LetterBonus>,
        wordMultiplier: Int,
    ): GameState {
        require(state.phase == GamePhase.PLAYING) { "Игра ещё не началась." }
        val score = ScoreCalculator.scoreWord(word, letterBonuses, wordMultiplier)
        val player = state.currentPlayer ?: error("Нет текущего игрока.")
        val updatedPlayers = state.players.mapIndexed { index, item ->
            if (index == state.currentPlayerIndex) {
                item.copy(score = item.score + score.total)
            } else {
                item
            }
        }
        val entry = HistoryEntry(
            round = state.round,
            playerIndex = state.currentPlayerIndex,
            playerName = player.name,
            word = score.normalizedWord,
            letterBonuses = letterBonuses.sortedBy { it.index },
            wordMultiplier = wordMultiplier,
            points = score.total,
        )
        return advanceTurn(state.copy(players = updatedPlayers, history = state.history + entry))
    }

    fun skipTurn(state: GameState): GameState {
        require(state.phase == GamePhase.PLAYING) { "Игра ещё не началась." }
        val player = state.currentPlayer ?: error("Нет текущего игрока.")
        val entry = HistoryEntry(
            round = state.round,
            playerIndex = state.currentPlayerIndex,
            playerName = player.name,
            word = null,
            letterBonuses = emptyList(),
            wordMultiplier = 1,
            points = 0,
        )
        return advanceTurn(state.copy(history = state.history + entry))
    }

    fun undoLastTurn(state: GameState): GameState {
        val last = state.history.lastOrNull() ?: return state
        val updatedPlayers = state.players.mapIndexed { index, player ->
            if (index == last.playerIndex) {
                player.copy(score = player.score - last.points)
            } else {
                player
            }
        }
        return state.copy(
            phase = GamePhase.PLAYING,
            players = updatedPlayers,
            currentPlayerIndex = last.playerIndex,
            round = last.round,
            history = state.history.dropLast(1),
        )
    }

    fun finishGame(state: GameState): GameState {
        require(state.players.isNotEmpty()) { "Нельзя завершить игру без игроков." }
        return state.copy(phase = GamePhase.FINISHED)
    }

    fun resetGame(): GameState = GameState()

    fun ranking(players: List<Player>): List<RankedPlayer> =
        players.sortedWith(compareByDescending<Player> { it.score }.thenBy { it.name.lowercase(Locale("ru")) })
            .mapIndexed { index, player ->
                RankedPlayer(index + 1, player.name, player.score)
            }

    private fun advanceTurn(state: GameState): GameState {
        val nextIndex = if (state.currentPlayerIndex == state.players.lastIndex) 0 else state.currentPlayerIndex + 1
        val nextRound = if (nextIndex == 0) state.round + 1 else state.round
        return state.copy(currentPlayerIndex = nextIndex, round = nextRound)
    }
}
