package about.util

import connect4.game.*

data class ColorValue(val c: Int, val y: Int, val m: Int, val k: Int)

fun InputType?.colorValue() =  -(this ?: InputType.DualNeutral).ordinal + (LayerSize.entries.size / 2)
fun LayerSize?.colorValue() =  -(this ?: LayerSize.Medium).ordinal + (LayerSize.entries.size / 2)
fun Activation?.colorValue() = -(this ?: Activation.Mish).ordinal + (Activation.entries.size / 2)
fun OutputActivation?.colorValue() = -(this ?: OutputActivation.Sigmoid).ordinal + (OutputActivation.entries.size / 2)
fun Padding?.colorValue() = (this?.let { it == Padding.Same }).colorValue()
fun Boolean?.colorValue() = when(this) {
    true -> 2
    false -> -2
    else -> 0
}

data class LimitedDescription(
    val inputType: InputType? = null,
    val batchNorm: Boolean? = null,
    val padding: Padding? = null,
    val convLayerSize: LayerSize? = null,
    val convLayerActivation: Activation? = null,
    val denseLayerSize: LayerSize? = null,
    val denseLayerActivation: Activation? = null,
    val output: OutputActivation? = null
)