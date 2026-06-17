package ru.slovodel.scorekeeper.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import ru.slovodel.scorekeeper.model.LetterBonus

class ScoreCalculatorTest {
    @Test
    fun simpleWordWithoutBonuses() {
        assertEquals(5, ScoreCalculator.scoreWord("кот").total)
    }

    @Test
    fun letterBonusTimesTwo() {
        assertEquals(6, ScoreCalculator.scoreWord("кот", listOf(LetterBonus(1, 2))).total)
    }

    @Test
    fun letterBonusTimesThree() {
        assertEquals(7, ScoreCalculator.scoreWord("кот", listOf(LetterBonus(1, 3))).total)
    }

    @Test
    fun severalLetterBonuses() {
        val result = ScoreCalculator.scoreWord(
            input = "дом",
            letterBonuses = listOf(LetterBonus(0, 2), LetterBonus(2, 3)),
        )

        assertEquals(11, result.total)
    }

    @Test
    fun sameLettersOnlySelectedOccurrenceGetsBonus() {
        val result = ScoreCalculator.scoreWord("мама", listOf(LetterBonus(1, 3)))

        assertEquals(8, result.total)
    }

    @Test
    fun wordBonusTimesTwo() {
        assertEquals(10, ScoreCalculator.scoreWord("кот", wordMultiplier = 2).total)
    }

    @Test
    fun wordBonusTimesThree() {
        assertEquals(15, ScoreCalculator.scoreWord("кот", wordMultiplier = 3).total)
    }

    @Test
    fun letterAndWordBonusesTogether() {
        assertEquals(18, ScoreCalculator.scoreWord("кот", listOf(LetterBonus(1, 2)), 3).total)
    }

    @Test
    fun yoLetterIsSupported() {
        assertEquals(8, ScoreCalculator.scoreWord("ёж").lettersSum)
        assertEquals(8, ScoreCalculator.scoreWord("ёж").total)
    }

    @Test
    fun uppercaseRussianLettersAreAccepted() {
        assertEquals(5, ScoreCalculator.scoreWord("КОТ").total)
    }

    @Test
    fun emptyStringIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ScoreCalculator.scoreWord("   ")
        }
    }

    @Test
    fun invalidCharactersAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ScoreCalculator.scoreWord("ко-т1")
        }
    }
}
