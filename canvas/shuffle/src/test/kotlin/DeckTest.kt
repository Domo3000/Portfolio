package projects.shuffle

import Deck
import kotlin.test.*

class DeckTest {
    @Test
    fun constructorTest() {
        assertEquals(
            Deck(4),
            Deck(listOf(1, 2, 3, 4))
        )
    }

    @Test
    fun toStringTest() {
        assertEquals(
            "{1,3,2}",
            Deck(listOf(1, 3, 2)).toString()
        )
    }

    @Test
    fun equalsTest() {
        assertEquals(
            Deck(4),
            Deck(listOf(1, 2, 3, 4))
        )
        assertNotEquals(
            Deck(4),
            Deck(listOf(4, 3, 2, 1))
        )
    }

    @Test
    fun sortedTest() {
        assertTrue(Deck(4).sorted())
        assertTrue(Deck(listOf(1, 2, 3, 4)).sorted())
        assertFalse(Deck(listOf(1, 4, 2, 3)).sorted())
    }

    @Test
    fun pile() {
        listOf(
            2 to mutableListOf(1, 3, 5, 7, 9, 2, 4, 6, 8),
            3 to mutableListOf(1, 4, 7, 2, 5, 8, 3, 6, 9),
            4 to mutableListOf(1, 5, 9, 2, 6, 3, 7, 4, 8),
            5 to mutableListOf(1, 6, 2, 7, 3, 8, 4, 9, 5),
            6 to mutableListOf(1, 7, 2, 8, 3, 9, 4, 5, 6),
            7 to mutableListOf(1, 8, 2, 9, 3, 4, 5, 6, 7),
            8 to mutableListOf(1, 9, 2, 3, 4, 5, 6, 7, 8)
        ).forEach { (n, list) ->
            for (i in 9 downTo 1) {
                val deck = Deck(i)
                deck.pile(n)
                assertEquals(
                    Deck(list),
                    deck
                )
                list.remove(i)
            }
        }
    }

    @Test
    fun pileEnds() {
        for (i in 9 downTo 1) {
            val deck = Deck(i)
            deck.pile(1)
            assertEquals(
                Deck(i),
                deck
            )
            deck.pile(9)
            assertEquals(
                Deck(i),
                deck
            )
        }
    }
}