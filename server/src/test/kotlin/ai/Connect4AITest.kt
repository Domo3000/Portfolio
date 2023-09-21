package ai

import connect4.ai.BattleHandler
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import org.junit.Test
import java.time.Instant
import kotlin.random.Random
import kotlin.time.measureTime

class Connect4AITest {
    private val random = Random(Instant.now().toEpochMilli())

    @Test
    fun battleLengthTestLength() {
        val battleHandler = BattleHandler(listOf(SimpleLengthAI(false, random.nextLong())))

        println(measureTime {
            repeat(100) {
                battleHandler.singleOpponentBattle { BalancedLengthAI(false, random.nextLong()) }
            }
        })

        println(measureTime {
            repeat(100) {
                battleHandler.singleOpponentBattle { BalancedLengthAI(true, random.nextLong()) }
            }
        })
    }

    @Test
    fun battleLengthTestMonte() {
        val battleHandler = BattleHandler(listOf(BalancedMonteCarloAI(1000, random.nextLong())))

        println(measureTime {
            battleHandler.singleOpponentBattle { BalancedMonteCarloAI(1000, random.nextLong()) }
        })
    }
}
