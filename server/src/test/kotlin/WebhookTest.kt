
import connect4.Connect4ConnectionHandler
import connect4.Connect4GameHandler
import connect4.installConnect4Websocket
import connect4.messages.Connect4Messages
import connect4.messages.ConnectedMessage
import io.ktor.client.plugins.websocket.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import io.ktor.client.plugins.websocket.WebSockets as CWebsockets

class WebhookTest {
    private fun webhookTest(url: String = "/connect4", block: suspend DefaultClientWebSocketSession.() -> Unit) {
        testApplication {
            application {
                with(Connect4ConnectionHandler) {
                    with(Connect4GameHandler) {
                        with(environment) {
                            install(WebSockets)
                            installConnect4Websocket()
                        }
                    }
                }
            }

            val client = createClient {
                install(CWebsockets)
            }

            client.webSocket(url) {
                block(this)
            }
        }
    }

    @Test
    fun webSocketTest() {
        webhookTest {
            send(Frame.Text("Test"))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val message = Connect4Messages.decode(responseText)
            assertNotNull(message as? ConnectedMessage)
        }
    }
}
