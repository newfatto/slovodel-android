package ru.slovodel.scorekeeper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.slovodel.scorekeeper.game.GameEngine
import ru.slovodel.scorekeeper.game.ScoreCalculator
import ru.slovodel.scorekeeper.model.GamePhase
import ru.slovodel.scorekeeper.model.GameState
import ru.slovodel.scorekeeper.model.HistoryEntry
import ru.slovodel.scorekeeper.model.LetterBonus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlovodelApp(controller: GameController) {
    var errorText by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Словодел") },
                actions = {
                    IconButton(onClick = { confirmReset = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Сбросить игру")
                    }
                },
            )
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (controller.state.phase) {
                GamePhase.SETUP -> SetupScreen(controller) { errorText = it }
                GamePhase.PLAYING -> GameScreen(controller) { errorText = it }
                GamePhase.FINISHED -> FinalScreen(controller)
            }
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Сбросить игру?",
            text = "Все игроки, очки и история ходов будут удалены.",
            onDismiss = { confirmReset = false },
            onConfirm = {
                confirmReset = false
                controller.resetGame()
            },
        )
    }

    errorText?.let { message ->
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text("Нужно исправить") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorText = null }) {
                    Text("Понятно")
                }
            },
        )
    }

}

@Composable
private fun SetupScreen(controller: GameController, showError: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    ContentColumn {
        Text("Игроки", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя игрока") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    runAction(showError) {
                        controller.addPlayer(name)
                        name = ""
                    }
                },
                modifier = Modifier.height(56.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }

        if (controller.state.players.isEmpty()) {
            EmptyPanel("Добавьте игроков перед началом партии.")
        } else {
            controller.state.players.forEachIndexed { index, player ->
                CardRow {
                    Text(
                        player.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = { controller.removePlayer(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить игрока")
                    }
                }
            }
        }

        Button(
            onClick = { runAction(showError) { controller.startGame() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Начать игру")
        }
    }
}

@Composable
private fun GameScreen(controller: GameController, showError: (String) -> Unit) {
    val state = controller.state
    var word by remember { mutableStateOf("") }
    var letterBonuses by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var wordMultiplier by remember { mutableStateOf(1) }
    var confirmFinish by remember { mutableStateOf(false) }
    val normalizedWord = runCatching { ScoreCalculator.normalizeWord(word) }.getOrNull()
    val bonusList = letterBonuses.map { LetterBonus(it.key, it.value) }.sortedBy { it.index }
    val preview = runCatching { ScoreCalculator.scoreWord(word, bonusList, wordMultiplier) }.getOrNull()

    LaunchedEffect(normalizedWord) {
        if (normalizedWord != null) {
            letterBonuses = letterBonuses.filterKeys { it in normalizedWord.indices }
        }
    }

    ContentColumn {
        GameHeader(state)

        OutlinedTextField(
            value = word,
            onValueChange = { word = it },
            label = { Text("Слово") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ScorePreview(preview?.total ?: 0, preview?.explanation.orEmpty())
        LetterBonusEditor(
            word = normalizedWord.orEmpty(),
            bonuses = letterBonuses,
            onChange = { index, multiplier ->
                letterBonuses = if (multiplier == 1) {
                    letterBonuses - index
                } else {
                    letterBonuses + (index to multiplier)
                }
            },
        )
        WordBonusEditor(wordMultiplier) { wordMultiplier = it }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    runAction(showError) {
                        controller.recordWord(word, bonusList, wordMultiplier)
                        word = ""
                        letterBonuses = emptyMap()
                        wordMultiplier = 1
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Записать")
            }
            OutlinedButton(
                onClick = {
                    controller.skipTurn()
                    word = ""
                    letterBonuses = emptyMap()
                    wordMultiplier = 1
                },
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Пропустить")
            }
        }

        ScoreTable(state)
        HistoryList(state.history)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { controller.undoLastTurn() },
                enabled = state.history.isNotEmpty(),
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Icon(Icons.Default.Undo, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Отменить")
            }
            Button(
                onClick = { confirmFinish = true },
                modifier = Modifier.weight(1f).height(52.dp),
            ) {
                Text("Закончить")
            }
        }
    }

    if (confirmFinish) {
        ConfirmDialog(
            title = "Закончить игру?",
            text = "После завершения будет показан итоговый рейтинг.",
            onDismiss = { confirmFinish = false },
            onConfirm = {
                confirmFinish = false
                controller.finishGame()
            },
        )
    }
}

@Composable
private fun FinalScreen(controller: GameController) {
    ContentColumn {
        Text("Итоги", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        GameEngine.ranking(controller.state.players).forEach { player ->
            ElevatedCard(colors = CardDefaults.elevatedCardColors()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${player.place}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(16.dp))
                    Text(player.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text("${player.score} очк.")
                }
            }
        }
        Button(
            onClick = { controller.resetGame() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Новая игра")
        }
    }
}

@Composable
private fun GameHeader(state: GameState) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Раунд ${state.round}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Ходит: ${state.currentPlayer?.name.orEmpty()}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ScorePreview(total: Int, explanation: List<String>) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Предварительно: $total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (explanation.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                explanation.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Composable
private fun LetterBonusEditor(
    word: String,
    bonuses: Map<Int, Int>,
    onChange: (Int, Int) -> Unit,
) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Бонусы букв", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (word.isEmpty()) {
                Text("Введите корректное слово, чтобы выбрать конкретные буквы.")
            } else {
                word.forEachIndexed { index, letter ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}. $letter", modifier = Modifier.weight(1f))
                        FilterChip(
                            selected = bonuses[index] == null,
                            onClick = { onChange(index, 1) },
                            label = { Text("нет") },
                        )
                        Spacer(Modifier.width(6.dp))
                        FilterChip(
                            selected = bonuses[index] == 2,
                            onClick = { onChange(index, 2) },
                            label = { Text("×2") },
                        )
                        Spacer(Modifier.width(6.dp))
                        FilterChip(
                            selected = bonuses[index] == 3,
                            onClick = { onChange(index, 3) },
                            label = { Text("×3") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordBonusEditor(selected: Int, onSelected: (Int) -> Unit) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Бонус слова", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "нет", 2 to "×2", 3 to "×3").forEach { (value, label) ->
                    FilterChip(
                        selected = selected == value,
                        onClick = { onSelected(value) },
                        label = { Text(label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreTable(state: GameState) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Результаты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            state.players.forEachIndexed { index, player ->
                val active = index == state.currentPlayerIndex
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (active) {
                        AssistChip(onClick = {}, label = { Text("ход") })
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Spacer(Modifier.width(56.dp))
                    }
                    Text(player.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${player.score}")
                }
            }
        }
    }
}

@Composable
private fun HistoryList(history: List<HistoryEntry>) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("История", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (history.isEmpty()) {
                Text("Пока ходов нет.")
            } else {
                history.asReversed().forEach { entry ->
                    Column {
                        Text(
                            "Раунд ${entry.round}: ${entry.playerName}",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(entry.word ?: "Пропуск хода")
                        Text("Буквы: ${formatBonuses(entry)} · Слово: ${formatWordBonus(entry.wordMultiplier)}")
                        Text("Очки: ${entry.points}")
                    }
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun ContentColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun CardRow(content: @Composable RowScope.() -> Unit) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun EmptyPanel(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Подтвердить")
            }
        },
    )
}

private fun runAction(showError: (String) -> Unit, action: () -> Unit) {
    runCatching(action).onFailure { showError(it.message ?: "Не удалось выполнить действие.") }
}

private fun formatBonuses(entry: HistoryEntry): String =
    if (entry.letterBonuses.isEmpty()) {
        "нет"
    } else {
        entry.letterBonuses.joinToString { "${it.index + 1}:×${it.multiplier}" }
    }

private fun formatWordBonus(multiplier: Int): String =
    if (multiplier == 1) "нет" else "×$multiplier"
