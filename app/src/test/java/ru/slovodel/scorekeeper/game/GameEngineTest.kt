package ru.slovodel.scorekeeper.game

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.slovodel.scorekeeper.model.GameState

class GameEngineTest {
    @Test
    fun finalRankingIsSortedByScoreAndThenName() {
        val state = GameEngine.addPlayer(GameState(), "Вера")
            .let { GameEngine.addPlayer(it, "Анна") }
            .let { GameEngine.addPlayer(it, "Борис") }
            .let { GameEngine.startGame(it) }
            .let { GameEngine.recordWord(it, "кот", emptyList(), 1) }
            .let { GameEngine.recordWord(it, "ёж", emptyList(), 1) }
            .let { GameEngine.recordWord(it, "кот", emptyList(), 1) }

        val ranking = GameEngine.ranking(state.players)

        assertEquals(listOf("Анна", "Борис", "Вера"), ranking.map { it.name })
    }

    @Test
    fun undoNormalTurnRestoresPlayerScoreAndTurn() {
        val state = twoPlayerGame()
            .let { GameEngine.recordWord(it, "кот", emptyList(), 1) }

        val undone = GameEngine.undoLastTurn(state)

        assertEquals(0, undone.players[0].score)
        assertEquals(0, undone.currentPlayerIndex)
        assertEquals(1, undone.round)
        assertEquals(0, undone.history.size)
    }

    @Test
    fun undoSkippedTurnRestoresPlayerAndRound() {
        val state = twoPlayerGame()
            .let { GameEngine.recordWord(it, "кот", emptyList(), 1) }
            .let { GameEngine.skipTurn(it) }

        val undone = GameEngine.undoLastTurn(state)

        assertEquals(1, undone.currentPlayerIndex)
        assertEquals(1, undone.round)
        assertEquals(1, undone.history.size)
    }

    @Test
    fun roundChangesAfterLastPlayerTurn() {
        val state = twoPlayerGame()
            .let { GameEngine.recordWord(it, "кот", emptyList(), 1) }
            .let { GameEngine.recordWord(it, "дом", emptyList(), 1) }

        assertEquals(0, state.currentPlayerIndex)
        assertEquals(2, state.round)
    }

    private fun twoPlayerGame(): GameState =
        GameEngine.addPlayer(GameState(), "Анна")
            .let { GameEngine.addPlayer(it, "Борис") }
            .let { GameEngine.startGame(it) }
}
