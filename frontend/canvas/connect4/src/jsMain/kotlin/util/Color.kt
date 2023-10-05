package util

import connect4.messages.Activation
import connect4.messages.LayerSize
import connect4.messages.NeuralDescription
import web.cssom.Color
import web.cssom.rgb

private fun LayerSize.colorValue() = -(this.ordinal - 2)

private fun Activation.colorValue() = -(this.ordinal - 2)

private fun List<Int>.colorValue() =
    127 + (64 * this.sumOf { it * (1 / this.size.toDouble()) })


fun NeuralDescription.color(): Color {
    val r = listOf(conv.size.colorValue(), conv.size.colorValue(), dense.activation.colorValue()).colorValue()
    val g =
        listOf(conv.activation.colorValue(), conv.activation.colorValue(), dense.activation.colorValue()).colorValue()
    val b = listOf(dense.size.colorValue(), dense.size.colorValue(), dense.activation.colorValue()).colorValue()

    return rgb(r.toInt(), g.toInt(), b.toInt())
}