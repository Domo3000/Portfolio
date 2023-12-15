package connect4.ai

import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.DefensiveLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.simple.BiasedRandomAI

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