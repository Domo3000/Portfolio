
import connect4.ai.length.SimpleLengthAI
import connect4.ai.simple.AlwaysSameAI
import connect4.ai.simple.BiasedRandomAI
import connect4.ai.simple.RandomAI
import kotlinx.datetime.Clock
import org.junit.Test
import utils.Moves
import utils.battlePlayers
import utils.evaluateBattles
import utils.evaluateWinningMoves
import kotlin.random.Random

class BaseScoreTest {

    @Test
    fun baseScore() {
        val random = Random(Clock.System.now().toEpochMilliseconds())
        val winningMoves = Moves.createWinningMoves(1000)

        ((0..6).map { { AlwaysSameAI(it) } } + listOf(
            { RandomAI(random.nextLong()) },
            { BiasedRandomAI(random.nextLong()) },
            { SimpleLengthAI(false, random.nextLong()) },
            { SimpleLengthAI(true, random.nextLong()) }
        )).forEach { ai ->
            println(ai().name)

            val scores = (0..10).map {
                val battleScore = evaluateBattles(ai(), battlePlayers(random))
                val winningMoveScore = evaluateWinningMoves(ai(), winningMoves)
                ((battleScore * 2.0) + winningMoveScore) / 3.0
            }

            println("min: ${scores.min()}")
            println("average: ${scores.average()}")
            println("max: ${scores.max()}")
            println(scores.max())
        }
    }
}
