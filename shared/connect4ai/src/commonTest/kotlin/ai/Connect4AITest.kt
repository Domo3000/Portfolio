package ai

import ai.length.BalancedLengthAI
import ai.length.SimpleLengthAI
import ai.monte.BalancedMonteCarloAI
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

    @Test
    fun battleLengthEvaluation() {
        val battleHandler = BattleHandler(listOf(SimpleLengthAI(false, random.nextLong())))

        val result = (0..10).map {
            var monte = 50

            val thousand = measureTime {
                repeat(1000) {
                    battleHandler.singleOpponentBattle { BalancedLengthAI(false, random.nextLong()) }
                }
            }

            while(measureTime {
                    battleHandler.singleOpponentBattle { BalancedMonteCarloAI(monte, random.nextLong()) }
                } < thousand) {
                monte += 50
            }

            println(monte)

            monte
        }.average()

        println(result)
    }
}
