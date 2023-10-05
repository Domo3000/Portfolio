package connect4.ai

import ai.length.BalancedLengthAI
import ai.length.DefensiveLengthAI
import ai.length.SimpleLengthAI
import ai.monte.BalancedMonteCarloAI
import ai.monte.MaximizeWinsMonteCarloAI
import ai.monte.MinimizeLossesMonteCarloAI
import ai.simple.BiasedRandomAI

object AIs {
    val simpleAIs = listOf(
        { BiasedRandomAI() },
        { MaximizeWinsMonteCarloAI(200, 6L) }
    )

    val mediumAIs = listOf(
        { BalancedLengthAI(false, 6L) },
        { DefensiveLengthAI(false, 6L) },
        { MinimizeLossesMonteCarloAI(350, 6L) }
    )

    val highAIs = listOf(
        { BalancedLengthAI(true, 6L) },
        { SimpleLengthAI(true, 6L) },
        { BalancedMonteCarloAI(700, 6L) }
    )

    val allNonNeurals = simpleAIs + mediumAIs + highAIs
}