package connect4.messages

import connect4.game.Player
import kotlin.test.Test
import kotlin.test.assertEquals

class Connect4MessagesTest {
    @Test
    fun serialize() {
        val messages: Set<Connect4Message> = setOf(
            ConnectedMessage(0),
            PickedAIMessage(AIChoice.Hard),
            PickedPlayerMessage(Player.FirstPlayer),
            GameStartedMessage(Player.FirstPlayer),
            GameFinishedMessage(Player.FirstPlayer),
            NextMoveMessage(0),
            WaitMessage,
            ContinueMessage
        )

        messages.forEach {
            assertEquals(
                expected = it,
                actual = Connect4Messages.decode(Connect4Messages.encode(it))
            )
        }
    }
}