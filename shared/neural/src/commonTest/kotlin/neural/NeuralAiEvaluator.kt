package neural

import ai.AI
import ai.length.SimpleLengthAI
import ai.monte.BalancedMonteCarloAI
import ai.simple.AlwaysSameAI
import connect4.game.Connect4Game
import org.junit.Test

data class NeuralAiChallenge(
    val game: Connect4Game,
    val expectedMove: List<Int>
) {
    fun print() {
        println(game.toString())
        println(expectedMove.joinToString(", "))
    }
}

object NeuralAiChallengeBuilder {
    fun empty(): NeuralAiChallenge = NeuralAiChallenge(Connect4Game(), listOf(3))

    // A
    // B
    // A
    fun verticalAlternating(index: Int, height: Int, game: Connect4Game): NeuralAiChallenge {
        repeat(height) {
            game.makeMove(index)
        }
        return NeuralAiChallenge(game, listOf(index))
    }

    // AB
    // AB
    // maxStartIndex = 5
    fun verticalPiles(startIndex: Int, height: Int, game: Connect4Game): NeuralAiChallenge {
        repeat(height) {
            game.makeMove(startIndex)
            game.makeMove(startIndex + 1)
        }
        return NeuralAiChallenge(game, listOf(startIndex, startIndex + 1))
    }

    // ABABABA
    // BABABAB
    // ABABABA
    // if height = 3 -> next player is B => 0, 2, 4, 6
    fun alternatingPilesEverywhere(height: Int, game: Connect4Game): NeuralAiChallenge {
        repeat(height) {
            (0..6).forEach {
                game.makeMove(it)
            }
        }
        val correct = if(height % 2 == 0) {
            listOf(0, 2, 4, 6)
        } else {
            listOf(1, 3, 5)
        }
        return NeuralAiChallenge(game, correct)
    }

    //   B
    //   B
    //   AA
    // maxStartIndex = 5
    fun horizontalL(startIndex: Int, game: Connect4Game): NeuralAiChallenge {
        game.makeMove(startIndex)
        game.makeMove(startIndex)
        game.makeMove(startIndex + 1)
        game.makeMove(startIndex)
        val expected = when (startIndex) {
            0 -> listOf(0, 2)
            5 -> listOf(4, 5)
            else -> listOf(startIndex - 1, startIndex, startIndex + 2)
        }
        return NeuralAiChallenge(game, expected)
    }

    //   B
    //   B
    //  AA
    // maxStartIndex = 5
    fun horizontalFlippedL(startIndex: Int, game: Connect4Game): NeuralAiChallenge {
        game.makeMove(startIndex + 1)
        game.makeMove(startIndex + 1)
        game.makeMove(startIndex)
        game.makeMove(startIndex + 1)
        val expected = when (startIndex) {
            0 -> listOf(1, 2)
            5 -> listOf(4, 6)
            else -> listOf(startIndex - 1, startIndex + 1, startIndex + 2)
        }
        return NeuralAiChallenge(game, expected)
    }

    //   B
    //   B
    //  AAA
    // maxStartIndex = 4
    fun horizontalT(startIndex: Int, game: Connect4Game): NeuralAiChallenge {
        game.makeMove(startIndex + 1)
        game.makeMove(startIndex + 1)
        game.makeMove(startIndex)
        game.makeMove(startIndex + 1)
        game.makeMove(startIndex + 2)
        val expected = when (startIndex) {
            0 -> listOf(3)
            4 -> listOf(3)
            else -> listOf(startIndex - 1, startIndex + 3)
        }
        return NeuralAiChallenge(game, expected)
    }

    fun completeChallenge(): List<NeuralAiChallenge> {
        val challenges = listOf(
            empty(),
            alternatingPilesEverywhere(2, Connect4Game()),
            alternatingPilesEverywhere(3, Connect4Game()),
            alternatingPilesEverywhere(4, Connect4Game()),
            alternatingPilesEverywhere(5, Connect4Game())
        ) + (0..5).map {
            verticalPiles(it, 3, Connect4Game())
        } + (0..4).map {
            horizontalT(it, Connect4Game())
        } + (0..5).map {
            horizontalL(it, Connect4Game())
        } + (0..5).map {
            horizontalFlippedL(it, Connect4Game())
        }  + (0..4).map {
            horizontalT(
                it,
                alternatingPilesEverywhere(2, Connect4Game()).game
            )
        } + (0..4).map {
            horizontalT(
                it,
                alternatingPilesEverywhere(3, Connect4Game()).game
            )
        }

        return challenges
    }
}

fun evaluate(ai: AI, challenge: NeuralAiChallenge, print: Boolean = false): Boolean {
    ai.reset()
    val game = challenge.game
    val move = ai.nextMove(game)
    if(print) {
        println(game.toString())
        println(move)
    }
    return challenge.expectedMove.contains(move)
}

class NeuralAiEvaluator {
    @Test
    fun testBuilder() {
        val empty = NeuralAiChallengeBuilder.empty()
        empty.print()
        (0..5).forEach {
            NeuralAiChallengeBuilder.verticalPiles(it, 3, Connect4Game()).print()
        }
        (0..4).forEach {
            NeuralAiChallengeBuilder.horizontalT(it, Connect4Game()).print()
        }
        (0..5).forEach {
            NeuralAiChallengeBuilder.horizontalL(it, Connect4Game()).print()
        }
        (0..5).forEach {
            NeuralAiChallengeBuilder.horizontalFlippedL(it, Connect4Game()).print()
        }
        val pilesEverywhere = NeuralAiChallengeBuilder.alternatingPilesEverywhere(3, Connect4Game())
        pilesEverywhere.print()
        val mixed = NeuralAiChallengeBuilder.horizontalT(2, pilesEverywhere.game)
        mixed.print()
        pilesEverywhere.print() // TODO game.copy() method at start of each
    }

    @Test
    fun completeChallengePrint() {
        NeuralAiChallengeBuilder.completeChallenge().forEach {
            it.print()
        }
    }

    @Test
    fun neuralChallenge() {
        val handler = StoredHandler()
        handler.loadStoredNeurals(silent = false)

        var max: Pair<Int, StoredNeuralAI>? = null

        handler.allNeurals().forEach { ai ->
            var aiScore = 0

            NeuralAiChallengeBuilder.completeChallenge().forEach {
                if (evaluate(ai, it)) {
                    aiScore++
                }
            }

            if(max == null || max!!.first < aiScore) {
                max = aiScore to ai
            }

            println("$aiScore: ${ai.info()}")
        }

        println(max!!.first)
        println(max!!.second.info())
    }

    @Test
    fun nonNeuralChallenge() {
        // TODO random.nextLong()
        ((0..6).map { AlwaysSameAI(it) } + listOf(SimpleLengthAI(false, 6L), BalancedMonteCarloAI(500, 6L))).forEach { ai ->
            var aiScore = 0

            NeuralAiChallengeBuilder.completeChallenge().forEach {
                if (evaluate(ai, it)) {
                    aiScore++
                }
            }

            println("$aiScore ${ai.name}")
        }
    }
}