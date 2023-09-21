package connect4.ai.neural

import connect4.game.sizeX
import connect4.game.sizeY
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input

object InputType {
    private fun inputLayer(dims: Long) = Input(
        sizeX.toLong(),
        sizeY.toLong(),
        dims
    )

    val OneDInput = { inputLayer(1L) }
    val TwoDInput = { inputLayer(2L) }

    fun toInput(inputSingular: Boolean?) = when(inputSingular) {
        true -> OneDInput()
        false -> TwoDInput()
        null -> null
    }

    fun toInput(inputSingular: Boolean) = when(inputSingular) {
        true -> OneDInput()
        false -> TwoDInput()
    }
}

