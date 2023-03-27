package connect4.ai.neural

import connect4.ai.AI
import connect4.game.Player
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.dl.api.core.SavingFormat
import org.jetbrains.kotlinx.dl.api.core.Sequential
import org.jetbrains.kotlinx.dl.api.core.WritingMode
import org.jetbrains.kotlinx.dl.dataset.Dataset
import org.jetbrains.kotlinx.dl.dataset.OnHeapDataset
import java.io.File

@Serializable
data class AdditionalInfo(
    val input: Boolean // TODO store random.info as well
)

private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "class"
}

class Move(val field: List<List<Player?>>, val move: Int)

fun List<List<Player?>>.toFloatArraySingular(player: Player): FloatArray {
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

fun List<List<Player?>>.toFloatArrayDual(player: Player): FloatArray {
    val p = normalize(player).flatten().map { element ->
        when (element) {
            player -> 1.0f
            else -> 0.0f
        }
    }
    val op = normalize(player).flatten().map { element ->
        when (element) {
            player.switch() -> 1.0f
            else -> 0.0f
        }
    }
    val x = p.zip(op).flatMap { listOf(it.first, it.second) }

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

fun List<Move>.toDataset(player: Player, inputSingular: Boolean): Dataset = OnHeapDataset.create(
    map { move ->
        if (inputSingular) {
            move.field.toFloatArraySingular(player)
        } else {
            move.field.toFloatArrayDual(player)
        }
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

abstract class NeuralAI(
    private val inputSingular: Boolean
) : AI() {
    abstract val brain: Sequential

    abstract override val name: String

    val epochs = 250

    fun nextMoveRanked(
        field: List<List<Player?>>,
        availableColumns: List<Int>,
        player: Player
    ): List<Pair<Int, Float>> {
        val floatArray = if (inputSingular) {
            field.toFloatArraySingular(player)
        } else {
            field.toFloatArrayDual(player)
        }

        return brain.predictSoftly(floatArray)
            .mapIndexed { column, estimate -> column to estimate }
            .filter { availableColumns.contains(it.first) }
            .sortedByDescending { it.second }
    }

    override fun nextMove(field: List<List<Player?>>, availableColumns: List<Int>, player: Player): Int =
        nextMoveRanked(field, availableColumns, player)
            .map { it.first }
            .first()

    fun train(list: List<Move>): Boolean {
        return try {
            brain.fit(
                list.toDataset(Player.FirstPlayer, inputSingular),
                epochs,
                100
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
            list.toDataset(Player.FirstPlayer, inputSingular)
        ).lossValue

    abstract fun info(): String

    fun store(path: String) {
        val baseDirectory = "${System.getProperty("user.dir")}/neurals"
        val directory = File("$baseDirectory/$path")
        if (!directory.isDirectory) {
            directory.mkdir()
        }
        brain.save(
            directory,
            SavingFormat.JSON_CONFIG_CUSTOM_VARIABLES,
            true,
            WritingMode.OVERRIDE,
        )
        val additional = File("$baseDirectory/$path/additionalInfo.json")
        if (additional.exists()) {
            additional.delete()
        }
        additional.createNewFile()
        additional.writeText(
            json.encodeToString(
                AdditionalInfo.serializer(),
                AdditionalInfo(inputSingular)
            )
        )
    }
}