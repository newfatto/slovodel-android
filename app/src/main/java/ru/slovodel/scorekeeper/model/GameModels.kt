package ru.slovodel.scorekeeper.model

enum class GamePhase {
    SETUP,
    PLAYING,
    FINISHED,
}

data class Player(
    val name: String,
    val score: Int = 0,
)

data class LetterBonus(
    val index: Int,
    val multiplier: Int,
)

data class HistoryEntry(
    val round: Int,
    val playerIndex: Int,
    val playerName: String,
    val word: String?,
    val letterBonuses: List<LetterBonus>,
    val wordMultiplier: Int,
    val points: Int,
)

data class GameState(
    val phase: GamePhase = GamePhase.SETUP,
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val round: Int = 1,
    val history: List<HistoryEntry> = emptyList(),
) {
    val currentPlayer: Player?
        get() = players.getOrNull(currentPlayerIndex)
}

data class RankedPlayer(
    val place: Int,
    val name: String,
    val score: Int,
)
