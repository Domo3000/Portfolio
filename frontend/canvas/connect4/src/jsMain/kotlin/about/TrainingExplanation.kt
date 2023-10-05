package about

import connect4.messages.Activation
import connect4.messages.LayerDescription
import connect4.messages.LayerSize
import connect4.messages.NeuralDescription
import react.FC
import react.Props

val TrainingExplanation = FC<Props> {
    // 5000 moves at 25 epochs
    val timeds = mapOf(
        NeuralDescription(LayerDescription(LayerSize.None, Activation.Relu), LayerDescription(LayerSize.None, Activation.Relu)) to ""
    )

    val times = listOf(
        // N
        0.05,
        0.1,
        0.2,
        0.2,
        0.6,
        // S
        0.2,
        0.4,
        0.6,
        0.7,
        1.0,
        // M
        0.4,
        0.7,
        0.8,
        1.2,
        1.5,
        // L
        1.8,
        2.0,
        2.1,
        2.5,
        2.8,
        // G
        7.1,
        7.2,
        7.3,
        7.6,
        8.3
    )

    LayerSize.entries.map { convSize ->
        LayerSize.entries.map { denseSize ->
            convSize to denseSize
        }
    }
}