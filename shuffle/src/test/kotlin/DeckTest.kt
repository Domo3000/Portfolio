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
    fun pile2() {
        val initialList = mutableListOf(1, 3, 5, 7, 9, 2, 4, 6, 8)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(2)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile3() {
        val initialList = mutableListOf(1, 4, 7, 2, 5, 8, 3, 6, 9)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(3)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile4() {
        val initialList = mutableListOf(1, 5, 9, 2, 6, 3, 7, 4, 8)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(4)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile5() {
        val initialList = mutableListOf(1, 6, 2, 7, 3, 8, 4, 9, 5)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(5)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile6() {
        val initialList = mutableListOf(1, 7, 2, 8, 3, 9, 4, 5, 6)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(6)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile7() {
        val initialList = mutableListOf(1, 8, 2, 9, 3, 4, 5, 6, 7)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(7)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile8() {
        val initialList = mutableListOf(1, 9, 2, 3, 4, 5, 6, 7, 8)
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(initialList),
                Deck(i).pile(8)
            )
            initialList.remove(i)
        }
    }

    @Test
    fun pile9() {
        for (i in 9 downTo 1) {
            assertEquals(
                Deck(i),
                Deck(i).pile(9)
            )
            assertEquals(
                Deck(i).pile(1),
                Deck(i).pile(9)
            )
        }
    }
}