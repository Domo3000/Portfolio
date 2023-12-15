package utils

import connect4.ai.length.AggressiveLengthAI
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.DefensiveLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.simple.BiasedRandomAI
import neural.StoredHandler
import neural.SwitchAI
import kotlin.random.Random

object Neurals {
    val neurals by lazy { StoredHandler.loadStored("server/neurals") }
}

fun trainingPlayers(random: Random) = listOf(
    { BiasedRandomAI(random.nextLong()) },
    { SwitchAI(Neurals.neurals, random.nextLong()) },
    { SwitchAI(Neurals.neurals.take(random.nextInt(3, 5)), random.nextLong()) },
    { SwitchAI(Neurals.neurals.take(random.nextInt(5, Neurals.neurals.size)), random.nextLong()) },
    { AggressiveLengthAI(true, random.nextLong()) },
    { DefensiveLengthAI(true, random.nextLong()) },
    { BalancedLengthAI(true, random.nextLong()) },
    { SimpleLengthAI(true, random.nextLong()) }
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
