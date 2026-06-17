package ru.slovodel.scorekeeper.game

import java.util.Locale
import ru.slovodel.scorekeeper.model.LetterBonus

data class LetterScore(
    val index: Int,
    val letter: Char,
    val baseScore: Int,
    val multiplier: Int,
) {
    val total: Int = baseScore * multiplier
}

data class ScoreResult(
    val normalizedWord: String,
    val letters: List<LetterScore>,
    val lettersSum: Int,
    val wordMultiplier: Int,
    val total: Int,
    val explanation: List<String>,
)

object ScoreCalculator {
    val letterScores: Map<Char, Int> = mapOf(
        'а' to 1,
        'б' to 3,
        'в' to 2,
        'г' to 3,
        'д' to 2,
        'е' to 1,
        'ё' to 1,
        'ж' to 7,
        'з' to 4,
        'и' to 1,
        'й' to 5,
        'к' to 2,
        'л' to 2,
        'м' to 2,
        'н' to 2,
        'о' to 1,
        'п' to 2,
        'р' to 2,
        'с' to 2,
        'т' to 2,
        'у' to 3,
        'ф' to 10,
        'х' to 5,
        'ц' to 8,
        'ч' to 5,
        'ш' to 8,
        'щ' to 9,
        'ъ' to 10,
        'ы' to 4,
        'ь' to 5,
        'э' to 9,
        'ю' to 8,
        'я' to 3,
    )

    fun normalizeWord(input: String): String {
        val word = input.trim().lowercase(Locale("ru"))
        require(word.isNotEmpty()) { "Введите слово." }
        require(word.all { it in letterScores }) {
            "Слово может содержать только русские буквы без пробелов, цифр и знаков."
        }
        return word
    }

    fun scoreWord(
        input: String,
        letterBonuses: List<LetterBonus> = emptyList(),
        wordMultiplier: Int = 1,
    ): ScoreResult {
        val word = normalizeWord(input)
        require(wordMultiplier in 1..3) { "Бонус слова должен быть ×1, ×2 или ×3." }

        val bonusesByIndex = letterBonuses.associateBy { it.index }
        require(bonusesByIndex.size == letterBonuses.size) {
            "Для одной буквы можно выбрать только один бонус."
        }
        letterBonuses.forEach { bonus ->
            require(bonus.index in word.indices) { "Выбранной буквы нет в слове." }
            require(bonus.multiplier == 2 || bonus.multiplier == 3) {
                "Бонус буквы должен быть ×2 или ×3."
            }
        }

        val letters = word.mapIndexed { index, letter ->
            val base = letterScores.getValue(letter)
            LetterScore(
                index = index,
                letter = letter,
                baseScore = base,
                multiplier = bonusesByIndex[index]?.multiplier ?: 1,
            )
        }
        val lettersSum = letters.sumOf { it.total }
        val total = lettersSum * wordMultiplier
        val explanation = buildList {
            letters.forEach { letter ->
                if (letter.multiplier == 1) {
                    add("${letter.letter} = ${letter.baseScore}")
                } else {
                    add("${letter.letter} = ${letter.baseScore} × ${letter.multiplier}")
                }
            }
            add("Сумма букв: $lettersSum")
            if (wordMultiplier > 1) {
                add("Слово × $wordMultiplier")
            } else {
                add("Без бонуса слова")
            }
            add("Итого: $total")
        }

        return ScoreResult(
            normalizedWord = word,
            letters = letters,
            lettersSum = lettersSum,
            wordMultiplier = wordMultiplier,
            total = total,
            explanation = explanation,
        )
    }
}
