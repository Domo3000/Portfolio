package neural

import connect4.ai.AI
import connect4.game.InputType
import connect4.game.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.dl.api.core.SavingFormat
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.WritingMode
import org.jetbrains.kotlinx.dl.api.core.layer.Layer
import org.jetbrains.kotlinx.dl.api.core.layer.convolutional.Conv2D
import org.jetbrains.kotlinx.dl.api.core.layer.core.Dense
import org.jetbrains.kotlinx.dl.api.core.layer.core.Input
import org.jetbrains.kotlinx.dl.api.core.layer.normalization.BatchNorm
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.AvgPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.pooling.MaxPool2D
import org.jetbrains.kotlinx.dl.api.core.layer.reshaping.Flatten
import org.jetbrains.kotlinx.dl.dataset.Dataset
import org.jetbrains.kotlinx.dl.dataset.OnHeapDataset
import java.io.File

class Move(val field: List<List<Player?>>, val move: Int)


private fun List<List<Player?>>.toFloatArraySingular(player: Player, opponent: Float): FloatArray {
    val x = normalize(player).flatten().map { element ->
        when (element) {
            player -> 1.0f
            null -> 0.0f
            else -> opponent
        }
    }
    return FloatArray(x.size) { index ->
        x[index]
    }
}

private fun List<List<Player?>>.toFloatArrayDual(player: Player, opponent: Float): FloatArray {
    val p = normalize(player).flatten().map { element ->
        when (element) {
            player -> 1.0f
            null -> 0.0f
            else -> opponent
        }
    }
    val op = normalize(player).flatten().map { element ->
        when (element) {
            player.switch() -> 1.0f
            null -> 0.0f
            else -> opponent
        }
    }
    val x = p.zip(op).flatMap { listOf(it.first, it.second) }

    return FloatArray(x.size) { index ->
        x[index]
    }
}

private fun List<List<Player?>>.toFloatArray(player: Player, inputType: InputType): FloatArray = when (inputType) {
    InputType.SingularMinus -> this.toFloatArraySingular(player, -1.0f)
    InputType.SingularPlus -> this.toFloatArraySingular(player, 2.0f)
    InputType.DualNeutral -> this.toFloatArrayDual(player, 0.0f)
    InputType.DualMinus -> this.toFloatArrayDual(player, -1.0f)
    InputType.DualPlus -> this.toFloatArrayDual(player, 2.0f)
}

private fun List<List<Player?>>.normalize(player: Player): List<List<Player?>> = map {
    it.toList().map { n ->
        if (Player.FirstPlayer != player) {
            n?.switch()
        } else {
            n
        }
    }
}

fun List<Move>.toDataset(player: Player, inputType: InputType): Dataset = OnHeapDataset.create(
    map { move ->
        move.field.toFloatArray(player, inputType)
    }.toTypedArray(),
    run {
        val x = map { move ->
            move.move.toFloat()
        }

        FloatArray(x.size) { index ->
            x[index]
        }
    }
)


private fun Layer.name(): String = when (this) {
    is Input -> "Input(${packedDims[2]})"
    is BatchNorm -> "BatchNorm()"
    is Dense -> "Dense($outputSize/${activation.name})"
    is Conv2D -> "Conv($filters/${kernelSize.contentToString()}/${activation.name}/$padding)"
    is AvgPool2D -> "Avg2D(${poolSize.contentToString()}/${strides.contentToString()}/$padding)"
    is MaxPool2D -> "Max2D(${poolSize.contentToString()}/${strides.contentToString()}/$padding)"
    is Flatten -> "Flatten()"
    else -> throw Exception("unhandled Layer")
}

@Serializable
data class AdditionalInfo(
    val inputType: InputType
)

// TODO move to some common place
val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "class"
}

abstract class NeuralAI : AI(null) {
    abstract val brain: Sequential
    abstract val inputType: InputType

    abstract override val name: String

    val epochs = 10


    fun nextMoveRanked(
        field: List<List<Player?>>,
        availableColumns: List<Int>,
        player: Player
    ): List<Pair<Int, Float>> = brain.predictSoftly(field.toFloatArray(player, inputType))
        .mapIndexed { column, estimate -> column to estimate }
        .filter { availableColumns.contains(it.first) }
        .sortedByDescending { it.second }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int =
        nextMoveRanked(field, availableColumns, player)
            .map { it.first }
            .first()

    fun train(list: List<Move>): Boolean {
        return try {
            brain.fit(
                list.toDataset(Player.FirstPlayer, inputType),
                epochs,
                1000
            )
            true
        } catch (e: java.lang.Exception) {
            println("Error in Training")
            println(e.message)
            false
        }
    }

    fun evaluate(list: List<Move>): Double =
        brain.evaluate(
            list.toDataset(Player.FirstPlayer, inputType)
        ).lossValue

    fun info() =
        "$name: ${brain.layers.joinToString(", ", "{ ", " }") { it.name() }}"

    fun paramsCount() = brain.summary().totalParamsCount

    fun store(path: String, name: String) {
        val directory = File("${System.getProperty("user.dir")}/$path/$name")
        if (!directory.isDirectory) {
            directory.mkdir()
        }
        brain.save(
            directory,
            SavingFormat.JSON_CONFIG_CUSTOM_VARIABLES,
            true,
            WritingMode.OVERRIDE,
        )
        val additional = File(directory.path + "/additionalInfo.json")
        if (additional.exists()) {
            additional.delete()
        }
        additional.createNewFile()
        additional.writeText(
            json.encodeToString(
                AdditionalInfo.serializer(),
                AdditionalInfo(inputType)
            )
        )
    }
}

class OverallHighestAI(private val neurals: List<NeuralAI>) : AI(null) {
    override val name: String =
        "OverallHighest" + neurals.joinToString(",", "(", ")") { it.name.removeSurrounding("StoredNeural(", ")") }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        return neurals.map {
            it.nextMoveRanked(field, availableColumns, player)
        }
            .fold(Array(7) { 0.0f }) { acc, list ->
                list.forEach {
                    acc[it.first] += it.second
                }
                acc
            }.mapIndexed { i, r -> i to r }.maxBy { it.second }.first
    }
}

class MostCommonAI(private val neurals: List<NeuralAI>) : AI(null) {
    override val name: String =
        "MostCommon" + neurals.joinToString(",", "(", ")") { it.name.removeSurrounding("StoredNeural(", ")") }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        return neurals.map {
            it.nextMoveRanked(field, availableColumns, player)
        }.map { list ->
            list.maxBy { it.second }.first
        }.groupingBy { it }.eachCount().maxBy { it.value }.key
    }
}

class SwitchAI(private val neurals: List<NeuralAI>, seed: Long) : AI(seed) {
    override val name: String = "Switch(${neurals.size})"

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int {
        val ai = when (random.nextInt(0, 5)) {
            0 -> neurals.random(random)

            in 1..2 -> OverallHighestAI(neurals.shuffled(random).take(random.nextInt(2, neurals.size)))

            else -> MostCommonAI(neurals.shuffled(random).take(random.nextInt(2, neurals.size)))
        }

        return ai.nextMove(field, availableColumns, player)
    }
}