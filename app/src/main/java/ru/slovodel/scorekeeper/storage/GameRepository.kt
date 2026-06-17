package ru.slovodel.scorekeeper.storage

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import ru.slovodel.scorekeeper.model.GamePhase
import ru.slovodel.scorekeeper.model.GameState
import ru.slovodel.scorekeeper.model.HistoryEntry
import ru.slovodel.scorekeeper.model.LetterBonus
import ru.slovodel.scorekeeper.model.Player

class GameRepository(context: Context) {
    private val preferences = context.getSharedPreferences("slovodel_state", Context.MODE_PRIVATE)

    fun load(): GameState {
        val raw = preferences.getString(KEY_STATE, null) ?: return GameState()
        return runCatching { decode(JSONObject(raw)) }.getOrDefault(GameState())
    }

    fun save(state: GameState) {
        preferences.edit().putString(KEY_STATE, encode(state).toString()).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_STATE).apply()
    }

    private fun encode(state: GameState): JSONObject = JSONObject()
        .put("phase", state.phase.name)
        .put("currentPlayerIndex", state.currentPlayerIndex)
        .put("round", state.round)
        .put("players", JSONArray().apply {
            state.players.forEach { player ->
                put(JSONObject().put("name", player.name).put("score", player.score))
            }
        })
        .put("history", JSONArray().apply {
            state.history.forEach { entry ->
                put(
                    JSONObject()
                        .put("round", entry.round)
                        .put("playerIndex", entry.playerIndex)
                        .put("playerName", entry.playerName)
                        .put("word", entry.word ?: JSONObject.NULL)
                        .put("wordMultiplier", entry.wordMultiplier)
                        .put("points", entry.points)
                        .put("letterBonuses", JSONArray().apply {
                            entry.letterBonuses.forEach { bonus ->
                                put(JSONObject().put("index", bonus.index).put("multiplier", bonus.multiplier))
                            }
                        }),
                )
            }
        })

    private fun decode(json: JSONObject): GameState {
        val players = json.getJSONArray("players").let { array ->
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                Player(item.getString("name"), item.getInt("score"))
            }
        }
        val history = json.getJSONArray("history").let { array ->
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                HistoryEntry(
                    round = item.getInt("round"),
                    playerIndex = item.getInt("playerIndex"),
                    playerName = item.getString("playerName"),
                    word = item.optString("word").takeUnless { item.isNull("word") },
                    letterBonuses = item.getJSONArray("letterBonuses").let { bonuses ->
                        List(bonuses.length()) { bonusIndex ->
                            val bonus = bonuses.getJSONObject(bonusIndex)
                            LetterBonus(bonus.getInt("index"), bonus.getInt("multiplier"))
                        }
                    },
                    wordMultiplier = item.getInt("wordMultiplier"),
                    points = item.getInt("points"),
                )
            }
        }
        return GameState(
            phase = GamePhase.valueOf(json.getString("phase")),
            players = players,
            currentPlayerIndex = json.getInt("currentPlayerIndex").coerceIn(0, players.lastIndex.coerceAtLeast(0)),
            round = json.getInt("round").coerceAtLeast(1),
            history = history,
        )
    }

    private companion object {
        const val KEY_STATE = "state"
    }
}
