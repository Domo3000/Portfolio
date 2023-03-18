import canvas.*
import connect4.game.Connect4Game
import connect4.game.Player
import connect4.game.sizeY
import connect4.messages.*
import css.Classes
import csstype.Auto
import csstype.Color
import csstype.NamedColor
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.FC
import react.Props
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML
import react.useEffectOnce
import react.useState
import connect4.game.sizeX as connect4SizeX
import connect4.game.sizeY as connect4SizeY

sealed interface State

sealed interface WithPlayer : State {
    val player: Player
}

sealed interface WithGame : WithPlayer {
    val game: Connect4Game
}

object DisconnectedState : State

object SelectPlayerState : State

class SelectAIState(override val player: Player) : WithPlayer {
    fun toGameState() = GameState(player, Connect4Game())
}

class GameState(
    override val player: Player,
    override val game: Connect4Game,
    var hover: Int? = null,
    var wait: Boolean = true
) : WithGame {
    fun toFinishedState(winner: Player?) = GameFinishedState(player, game, winner)
}

class GameFinishedState(
    override val player: Player,
    override val game: Connect4Game,
    val winner: Player?
) : WithGame {
    fun toGameState() = GameState(player, game)
}

class StateHolder(var state: State = DisconnectedState)

class Connect4(host: String, port: Int, secure: Boolean) : ExternalCanvas() {
    override val name: String = "Connect4"

    private val websocketState = WebsocketState(host, port, secure)
    private var frameId: Int? = null

    override val component: FC<Props>
        get() = FC {
            val (holder, _) = useState(StateHolder())
            val (connectState, _) = useState(websocketState)

            val choices = listOf(
                AIChoice.Simple,
                AIChoice.Medium,
                AIChoice.Hard,
                AIChoice.Neural,
                AIChoice.Length,
                AIChoice.MonteCarlo
            )

            fun gameState() = holder.state as? GameState
            fun aiState() = holder.state as? SelectAIState

            fun toColor(player: Player?) = when (player) {
                Player.FirstPlayer -> NamedColor.firebrick
                Player.SecondPlayer -> NamedColor.gold
                else -> NamedColor.white
            }

            fun drawSelectPlayerScreen() {
                renderingContext.drawCircle(
                    canvasElement.width / 3.0,
                    canvasElement.height / 2.0,
                    canvasElement.width / 7.0,
                    toColor(Player.FirstPlayer)
                )
                renderingContext.drawCircle(
                    canvasElement.width / 3.0 * 2.0,
                    canvasElement.height / 2.0,
                    canvasElement.width / 7.0,
                    toColor(Player.SecondPlayer)
                )
                renderingContext.fillStyle = NamedColor.black
                val fontSize = canvasElement.height / 20
                renderingContext.font = "${fontSize}px Courier New"
                renderingContext.textAlign = CanvasTextAlign.CENTER
                renderingContext.fillText(
                    "First",
                    canvasElement.width / 3.0,
                    canvasElement.height / 2.0
                )
                renderingContext.fillText(
                    "Second",
                    canvasElement.width / 3.0 * 2.0,
                    canvasElement.height / 2.0
                )
            }

            fun circleWithText(text: String, x: Int, y: Int, color: Color) {
                renderingContext.drawCircle(
                    canvasElement.width / 4.0 * x,
                    canvasElement.height / 3.0 * y,
                    canvasElement.width / 9.0,
                    color
                )
                renderingContext.fillText(text, canvasElement.width / 4.0 * x, canvasElement.height / 3.0 * y)
            }

            fun drawSelectAIScreen(state: SelectAIState) {
                renderingContext.fillStyle = NamedColor.black
                val fontSize = canvasElement.height / 30
                renderingContext.font = "${fontSize}px Courier New"
                renderingContext.textAlign = CanvasTextAlign.CENTER // TODO centeredText helper functions
                (0..2).map { x ->
                    (0..1).map { y ->
                        circleWithText(choices[x + 3 * y].toString(), x + 1, y + 1, toColor(state.player.switch()))
                    }
                }
            }

            fun drawGame(state: GameState) {
                state.game.field.mapIndexed { y, row ->
                    row.mapIndexed { x, player ->
                        val relX =
                            canvasElement.getRelativeX(x, connect4SizeX) // TODO switch rel and abs everywhere
                        val relY = canvasElement.getRelativeY(y + 1, connect4SizeY + 1)
                        val width = canvasElement.getElementWidth(connect4SizeX)
                        renderingContext.fillStyle = NamedColor.mediumblue
                        renderingContext.fillRect(
                            relX, relY, width, width
                        )
                        val color = toColor(player)
                        renderingContext.drawCircle(relX + (width / 2), relY + (width / 2), width * 0.45, color)
                    }
                }
                if (!state.wait) {
                    state.hover?.let {
                        val relX = canvasElement.getRelativeX(it, connect4SizeX)
                        val relY = canvasElement.getRelativeY(0, connect4SizeY + 1)
                        val width = canvasElement.getElementWidth(connect4SizeX)
                        val color = toColor(state.player)
                        renderingContext.drawCircle(relX + (width / 2), relY + (width / 2), width * 0.45, color)
                    }
                }
            }

            fun drawFinished(state: GameFinishedState) {
                drawGame(state.toGameState())
                val text = when (state.winner) {
                    state.player -> "You won!"
                    state.player.switch() -> "You lost!"
                    else -> "Draw..."
                }

                renderingContext.fillStyle = NamedColor.black
                val fontSize = canvasElement.height / 20
                renderingContext.font = "${fontSize}px Courier New"
                renderingContext.textAlign = CanvasTextAlign.CENTER
                renderingContext.fillText(text, canvasElement.width / 2.0, canvasElement.height / (sizeY * 2.0))
            }

            fun drawConnecting() {
                renderingContext.fillStyle = NamedColor.black
                val fontSize = canvasElement.height / 20
                renderingContext.font = "${fontSize}px Courier New"
                renderingContext.fillText("Connection issues. Please refresh!", 10.0, canvasElement.height / 2.0)
            }

            fun drawState() {
                connectState.session?.let {
                    when (holder.state) {
                        DisconnectedState -> drawConnecting()
                        SelectPlayerState -> drawSelectPlayerScreen()
                        is SelectAIState -> drawSelectAIScreen(holder.state as SelectAIState)
                        is GameState -> drawGame(holder.state as GameState)
                        is GameFinishedState -> drawFinished(holder.state as GameFinishedState)
                    }
                } ?: run {
                    drawConnecting()
                }
            }

            fun draw() {
                canvasElement.resetDimensions(1.0)
                renderingContext.clear()
                drawState()
            }

            val resizeHandler: (Event) -> Unit = {
                draw()
            }

            fun makeMove(state: GameState, column: Int) {
                if (!state.wait && state.game.availableColumns.contains(column)) {
                    websocketState.inputMessage(NextMoveMessage(column))
                    state.wait = true
                }
            }

            fun handleMessages(message: Connect4Message) {
                console.log(message)
                when (message) {
                    is ConnectedMessage -> {
                        holder.state = SelectPlayerState
                    }

                    is PickedPlayerMessage -> {
                        holder.state = SelectAIState(message.player)
                    }

                    is PickedAIMessage -> {
                        val gameState = aiState()!!.toGameState()
                        holder.state = gameState
                        websocketState.inputMessage(GameStartedMessage(gameState.player))
                    }

                    is GameStartedMessage -> {
                        //holder.state = GameState(message.player, )
                    }

                    is NextMoveMessage -> {
                        gameState()?.game?.makeMove(message.column)
                    }

                    is WaitMessage -> {
                        gameState()?.wait = true
                    }

                    is ContinueMessage -> {
                        gameState()?.wait = false
                    }

                    is GameFinishedMessage -> {
                        gameState()?.let {
                            holder.state = gameState()!!.toFinishedState(message.player)
                        }
                    }

                    else -> {}
                }
            }

            fun run() {
                websocketState.readMessages { handleMessages(it) }
                draw()
                frameId = window.requestAnimationFrame { run() }
            }

            val keypressEventHandler: (Event) -> Unit = { event ->
                val key = (event as KeyboardEvent).key.lowercase()
                when (holder.state) {
                    is GameState -> when (key) {
                        in ("1".."7") -> {
                            makeMove((holder.state as GameState), key.toInt() - 1)
                        }
                    }

                    is SelectPlayerState -> when (key) {
                        in ("1".."2") -> {
                            val player = if (key.toInt() == 1) {
                                Player.FirstPlayer
                            } else {
                                Player.SecondPlayer
                            }
                            websocketState.inputMessage(PickedPlayerMessage(player))
                        }
                    }

                    is SelectAIState -> when (key) {
                        in ("1".."6") -> {
                            websocketState.inputMessage(PickedAIMessage(choices[key.toInt() - 1]))
                        }
                    }

                    else -> {}
                }
            }

            fun mouseEventHandler(event: MouseEvent<HTMLCanvasElement, *>, handler: (Double, Double) -> Unit) {
                val bounds = canvasElement.getBoundingClientRect()
                val x = event.clientX - bounds.left
                val y = event.clientY - bounds.top
                handler(x, y)
            }

            ReactHTML.div {
                css {
                    maxWidth = 800.px
                    margin = Auto.auto
                }
                ReactHTML.canvas {
                    css(Classes.canvas)
                    id = canvasId
                    onMouseMove = { event ->
                        if (holder.state is GameState) {
                            mouseEventHandler(event) { x, _ ->
                                val relativeXs = (0..7).map { it to canvasElement.getRelativeX(it, connect4SizeX) }
                                relativeXs.zipWithNext().find { it.first.second < x && it.second.second > x }
                                    ?.let { gameState()!!.hover = it.first.first } ?: run { gameState()!!.hover = null }
                            }
                        }
                    }
                    onClick = { event ->
                        when (holder.state) {
                            is SelectPlayerState -> {
                                mouseEventHandler(event) { x, _ ->
                                    val selected = if (x < canvasElement.width / 2.0) {
                                        Player.FirstPlayer
                                    } else {
                                        Player.SecondPlayer
                                    }
                                    websocketState.inputMessage(PickedPlayerMessage(selected))
                                }
                            }

                            is SelectAIState -> {
                                mouseEventHandler(event) { x, y ->
                                    val relativeXs = (0..3).map { it to canvasElement.getRelativeX(it, 3) }

                                    val selectedX = relativeXs.zipWithNext()
                                        .find { it.first.second < x && it.second.second > x }?.second?.first
                                        ?: 1

                                    val selectedY = if (y < canvasElement.height / 2.0) {
                                        0
                                    } else {
                                        1
                                    }

                                    websocketState.inputMessage(PickedAIMessage(choices[selectedX + (3 * selectedY) - 1]))
                                }
                            }

                            is GameFinishedState -> {
                                holder.state = SelectPlayerState
                            }

                            is GameState -> {
                                mouseEventHandler(event) { x, _ ->
                                    val relativeXs = (0..7).map { it to canvasElement.getRelativeX(it, connect4SizeX) }
                                    relativeXs.zipWithNext().find { it.first.second < x && it.second.second > x }
                                        ?.let { makeMove(holder.state as GameState, it.first.first) }
                                }
                            }

                            else -> {}
                        }
                    }
                }

                useEffectOnce {
                    canvasElement.setDimensions(800, 800)
                    addEventListener("resize" to resizeHandler)
                    addEventListener("keypress" to keypressEventHandler)
                    draw()
                    websocketState.connectSession()
                    frameId = window.requestAnimationFrame { run() }
                }
            }
        }

    private fun clearInterval() {
        frameId?.let { window.cancelAnimationFrame(it) }
        frameId = null
    }

    override fun cleanUp() {
        clearInterval()
        websocketState.closeSession()
    }

    override fun initialize() {}

    init {
        initEventListeners()
    }
}