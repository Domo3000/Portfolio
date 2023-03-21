import connect4.ai.neural.EvolutionHandler
import connect4.ai.neural.RandomNeuralAI
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
    // TODO produce games and expected move
    // eg. empty to 3
    // vertical line to 0 etc
    // generator for
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
    fun alternatingPilesEverywhere(game: Connect4Game): NeuralAiChallenge {
        repeat(3) {
            (0..6).forEach {
                game.makeMove(it)
            }
        }
        return NeuralAiChallenge(game, listOf(0, 2, 4, 6)) // TODO adjustable height and expectedMoves
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
}

private fun evaluate(ai: RandomNeuralAI, challenge: NeuralAiChallenge): Boolean {
    val game = challenge.game
    val move = ai.nextMove(game.field, game.availableColumns, game.currentPlayer)
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
        val pilesEverywhere = NeuralAiChallengeBuilder.alternatingPilesEverywhere(Connect4Game())
        pilesEverywhere.print()
        val mixed = NeuralAiChallengeBuilder.horizontalT(2, pilesEverywhere.game)
        mixed.print()
    }

    // TODO also block some lines with verticalAlternating, eg block middle 3
    @Test
    fun neuralChallenge() {
        val handler = EvolutionHandler()

        val pilesEverywhere = NeuralAiChallengeBuilder.alternatingPilesEverywhere(Connect4Game())
        val mixed = NeuralAiChallengeBuilder.horizontalT(2, pilesEverywhere.game)

        val challenges = listOf(
            NeuralAiChallengeBuilder.empty(),
            pilesEverywhere,
            mixed
        ) + (0..5).map {
            NeuralAiChallengeBuilder.verticalPiles(it, 3, Connect4Game())
        } + (0..4).map {
            NeuralAiChallengeBuilder.horizontalT(it, Connect4Game())
        } + (0..5).map {
            NeuralAiChallengeBuilder.horizontalL(it, Connect4Game())
        } + (0..5).map {
            NeuralAiChallengeBuilder.horizontalFlippedL(it, Connect4Game())
        }

        println("maxScore: ${challenges.count()}")

        handler.allNeurals().forEach { neuralCounter ->
            val ai = neuralCounter.ai
            var aiScore = 0

            challenges.forEach {
                if (evaluate(ai, it)) {
                    aiScore++
                }
            }

            println("$aiScore: ${ai.info()}")
        }
    }
}