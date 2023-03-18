package connect4.ai

import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.monte.MonteCarloAI
import connect4.ai.simple.BiasedRandomAI
import connect4.game.Connect4Player

abstract class AI : Connect4Player()

object AIs {
    val simpleAIs = listOf(
        { BiasedRandomAI() },
        { DumbLengthAI() },
        { MaximizeWinsMonteCarloAI(200) }
    )

    val mediumAIs = listOf(
        { BalancedLengthAI() },
        { DefensiveLengthAI() },
        { MinimizeLossesMonteCarloAI(350) }
    )

    val highAIs = listOf(
        { PlyLengthAI() },
        { SimpleLengthAI() },
        { BalancedMonteCarloAI(700) }
    )

    val allNonNeurals = simpleAIs + mediumAIs + highAIs
}