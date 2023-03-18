package connect4.ai.neural

import connect4.ai.AI
import connect4.game.Player
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.loss.Losses
import org.jetbrains.kotlinx.dl.api.core.metric.Metrics
import org.jetbrains.kotlinx.dl.api.core.optimizer.Adam
import org.jetbrains.kotlinx.dl.api.core.optimizer.ClipGradientByValue
import org.jetbrains.kotlinx.dl.dataset.Dataset
import org.jetbrains.kotlinx.dl.dataset.OnHeapDataset

class Move(val field: List<List<Player?>>, val move: Int)

fun List<List<Player?>>.toFloatArray(player: Player): FloatArray {
    val x = normalize(player).flatten().map { element ->
        when (element) {
            player -> 1.0f
            null -> 0.0f
            else -> -1.0f
        }
    }
    return FloatArray(x.size) { index ->
        x[index]
    }
}

fun List<List<Player?>>.normalize(player: Player): List<List<Player?>> = map {
    it.toList().map { n ->
        if (Player.FirstPlayer != player) {
            n?.switch()
        } else {
            n
        }
    }
}

// TODO find a smoother way to do this
fun List<Move>.pad(): List<Move> {
    return if(size > 8) {
        val last8 = takeLast(8)
        val last4 = takeLast(4)
        val last2 = takeLast(2)
        val last = last()
        this + last8 + last4 + last2 + last
    } else {
        this
    }
}

fun List<Move>.toDataset(player: Player): Dataset = OnHeapDataset.create(
    pad().map { move ->
        move.field.toFloatArray(player)
    }.toTypedArray(),
    run {
        val x = pad().map { move ->
            move.move.toFloat()
        }

        FloatArray(x.size) { index ->
            x[index]
        }
    }
)

abstract class NeuralAI(val training: List<Move>) : AI() {
    override val name: String = javaClass.simpleName

    val epochs = 500

    fun nextMoveRanked(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): List<Pair<Int, Float>> =
        brain.predictSoftly(field.toFloatArray(player))
            .mapIndexed { column, estimate -> column to estimate }
            .filter { availableColumns.contains(it.first) }
            .sortedByDescending { it.second }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int =
        nextMoveRanked(field, availableColumns, player)
            .map { it.first }
            .first()

    fun train(list: List<Move>, e: Int = epochs) {
        brain.fit(
            list.toDataset(Player.FirstPlayer),
            e
        )
    }

    abstract val brain: Sequential

    open fun initialize() {
        brain.compile(
            optimizer = Adam(clipGradient = ClipGradientByValue(0.1f)),
            loss = Losses.SOFT_MAX_CROSS_ENTROPY_WITH_LOGITS,
            metric = Metrics.ACCURACY
        )

        brain.init()

        brain.fit(
            training.toDataset(Player.FirstPlayer),
            epochs,
            1000
        )
    }
}