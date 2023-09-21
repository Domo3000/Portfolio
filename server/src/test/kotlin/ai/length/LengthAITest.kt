package ai.length

import ai.neural.NeuralAiChallengeBuilder
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.game.Connect4Game
import connect4.game.Player
import org.junit.Test

class LengthAITest {

    @Test
    fun evaluateScore() {
        val ais = listOf(true, false)
            .flatMap { listOf(SimpleLengthAI(it, 0L), BalancedLengthAI(it, 0L)) }

        val score = ais.associateWith { 0 }.toMutableMap()

        NeuralAiChallengeBuilder.completeChallenge().forEach { challenge ->
            val game = challenge.game
            challenge.print()
            ais.map { ai ->
                val passed = challenge.expectedMove.contains(ai.nextMove(challenge.game))
                println("${ai.name}: $passed")
                println(
                    ai.getScoresForPlayer(game.field, game.availableColumns, Player.FirstPlayer)
                        .joinToString(", ", "{", "}") {
                            "${it.column}: ${it.points}"
                        })
                println(
                    ai.getScoresForPlayer(game.field, game.availableColumns, Player.SecondPlayer)
                        .joinToString(", ", "{", "}") {
                            "${it.column}: ${it.points}"
                        })
                score[ai] = score[ai]!! + 1
            }
        }

        println(score)

        val game = Connect4Game()
        NeuralAiChallengeBuilder.horizontalT(0, game)
        NeuralAiChallengeBuilder.horizontalT(5, game)
        println(game.toString())
    }

    @Test
    fun evaluateSpecific() {
        val ais = listOf(true, false)
            .flatMap { listOf(SimpleLengthAI(it, 0L), BalancedLengthAI(it, 0L)) }

        val game = Connect4Game()
        NeuralAiChallengeBuilder.horizontalT(0, game)
        NeuralAiChallengeBuilder.verticalAlternating(0, 1, game)
        NeuralAiChallengeBuilder.horizontalT(4, game)
        println(game.toString())

        ais.map { ai ->
            println(ai.name)
            println(
                ai.getScoresForPlayer(game.field, game.availableColumns, Player.FirstPlayer)
                    .joinToString(", ", "{", "}") {
                        "${it.column}: ${it.points}"
                    })
            println(
                ai.getScoresForPlayer(game.field, game.availableColumns, Player.SecondPlayer)
                    .joinToString(", ", "{", "}") {
                        "${it.column}: ${it.points}"
                    })
        }
    }
}