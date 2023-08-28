import automaton.BooleanArray
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO improve
class AutomatonTest {
    @Test
    fun getValueTest() {
        val array = BooleanArray(2, 2)

        array.flip(1, 1)

        assertEquals(
            5,
            array.getParentValue(0, 0, null)
        )

        assertEquals(
            0,
            array.getParentValue(0, 0, false)
        )

        assertEquals(
            7,
            array.getParentValue(0, 0, true)
        )

        assertEquals(
            0,
            array.getParentValue(0, 1, null)
        )

        assertEquals(
            0,
            array.getParentValue(0, 1, false)
        )

        assertEquals(
            4,
            array.getParentValue(0, 1, true)
        )
    }

    @Test
    fun addRowTest() {
        val array = BooleanArray(2, 2)

        array.flip(0, 1)

        assertEquals(
            0,
            array.getParentValue(0, 1, false)
        )

        array.addRow()

        assertEquals(
            2,
            array.getParentValue(0, 1, false)
        )
    }
}