package connect4.messages

import kotlin.test.Test
import kotlin.test.assertEquals

class Connect4MessagesTest {
    @Test
    fun serialize() {
        val messages: Set<Connect4Message> = setOf(NextMoveMessage(0, 0), ConnectedMessage(0))

        messages.forEach {
            assertEquals(
                expected = it,
                actual = Connect4Messages.decode(Connect4Messages.encode(it))
            )
        }
    }
}