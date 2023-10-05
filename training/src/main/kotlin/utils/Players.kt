package utils

import ai.AI
import ai.length.AggressiveLengthAI
import ai.length.BalancedLengthAI
import ai.length.DefensiveLengthAI
import ai.length.SimpleLengthAI
import ai.monte.BalancedMonteCarloAI
import ai.simple.BiasedRandomAI
import neural.MostCommonAI
import neural.OverallHighestAI
import neural.StoredHandler
import kotlin.random.Random

object Neurals {
    private val neurals by lazy { StoredHandler.loadStored(prefix = "load") }

    fun player(random: Random): AI {
        val ai = when (random.nextInt(0, 5)) {
            0 -> neurals.random(random)

            in 1..2 -> OverallHighestAI(neurals.shuffled(random).take(3))

            else -> MostCommonAI(neurals.shuffled(random).take(3))
        }

        return ai
    }
}

fun trainingPlayers(random: Random) = listOf(
    { Neurals.player(random) },
    { AggressiveLengthAI(true, random.nextLong()) },
    { DefensiveLengthAI(true, random.nextLong()) },
    { BalancedLengthAI(true, random.nextLong()) },
    { SimpleLengthAI(true, random.nextLong()) },
    { BalancedMonteCarloAI(500, random.nextLong()) }
).shuffled()

fun battlePlayers(random: Random) = listOf(
    { BiasedRandomAI(random.nextLong()) },
    { AggressiveLengthAI(false, random.nextLong()) },
    { DefensiveLengthAI(false, random.nextLong()) },
    { BalancedLengthAI(false, random.nextLong()) },
    { SimpleLengthAI(false, random.nextLong()) },
    { AggressiveLengthAI(true, random.nextLong()) },
    { DefensiveLengthAI(true, random.nextLong()) },
    { BalancedLengthAI(true, random.nextLong()) },
    { SimpleLengthAI(true, random.nextLong()) }
)
