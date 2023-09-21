package ai.neural

import connect4.ai.AI
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.neural.Move
import connect4.ai.neural.NeuralAI
import connect4.ai.neural.StoredHandler
import connect4.ai.neural.StoredNeuralAI
import connect4.ai.simple.BiasedRandomAI
import connect4.game.Connect4Game
import connect4.game.Player
import org.junit.BeforeClass
import org.junit.Test
import kotlin.random.Random
import kotlin.time.measureTime

// TODO test
private fun allPossibleWinningMoves(game: Connect4Game): List<Move> = game.availableColumns.map { column ->
    val maybeWinningGame = Connect4Game(game.field, game.currentPlayer)
    maybeWinningGame.makeMove(column)
    if (maybeWinningGame.hasFinished()) {
        Move(game.field.map { it.toList() }, column)
    } else {
        null
    }
}.filterNotNull()

fun createWinningMoves(firstPlayer: AI, secondPlayer: AI, count: Int = 100) =
    createMoves(firstPlayer, secondPlayer, count / 2) + createMoves(secondPlayer, firstPlayer, count / 2)

private fun createMoves(firstPlayer: AI, secondPlayer: AI, count: Int = 100): List<Pair<Move, Player>> {
    val moves = mutableListOf<Pair<Move, Player>>()

    while (moves.size < count) {
        val game = Connect4Game()
        firstPlayer.reset()
        secondPlayer.reset()

        while (!game.hasFinished()) {
            moves.addAll(allPossibleWinningMoves(game).map { it to Player.FirstPlayer })
            val firstMove = game.makeMove(firstPlayer)
            firstPlayer.updateMove(firstMove, game.availableColumns)
            secondPlayer.updateMove(firstMove, game.availableColumns)
            if (!game.hasFinished()) {
                moves.addAll(allPossibleWinningMoves(game).map { it to Player.SecondPlayer })
                val secondMove = game.makeMove(secondPlayer)
                if (!game.hasFinished()) {
                    firstPlayer.updateMove(secondMove, game.availableColumns)
                    secondPlayer.updateMove(secondMove, game.availableColumns)
                }
            }
        }
    }

    return moves.toList()
}

class NeuralAiWinningMovesChallenge {
    private val random = Random(666) //TODO

    companion object {
        private val handler = StoredHandler()

        @BeforeClass
        @JvmStatic
        fun init() {
            handler.loadStoredNeurals(silent = true)
        }
    }

    private fun neuralAiWinningMovesChallenge(block: (NeuralAI, Connect4Game, Move) -> Int) {
        val moves = createWinningMoves(BalancedLengthAI(true, random.nextLong()), BiasedRandomAI()) +
                createWinningMoves(BalancedLengthAI(true, random.nextLong()), BalancedMonteCarloAI(300, 6L)) +
                createWinningMoves(SimpleLengthAI(false, random.nextLong()), BalancedLengthAI(true, random.nextLong()))
        val scores: MutableList<Pair<Int, StoredNeuralAI>> = mutableListOf()

        handler.allNeurals().forEach { ai ->
            var aiScore = 0

            moves.forEach { (move, player) ->
                val game = Connect4Game(move.field, player)

                aiScore += block(ai, game, move)
            }

            scores.add(aiScore to ai)
        }

        scores.sortedByDescending { it.first }.forEach { (score, ai) ->
            println("$score: ${ai.info()}")
        }
    }

    @Test
    fun createMoveTime() {
        println(measureTime {
            createMoves(BalancedLengthAI(true, random.nextLong()), BiasedRandomAI())
        })
        println(measureTime {
            createMoves(SimpleLengthAI(false, random.nextLong()), BalancedLengthAI(true, random.nextLong()))
        })
        println(measureTime {
            createMoves(BalancedMonteCarloAI(500, 6L), BalancedLengthAI(true, random.nextLong()))
        })
        println(measureTime {
            createMoves(BalancedMonteCarloAI(500, 6L), BiasedRandomAI())
        })
    }

    @Test
    fun neuralAiWinningMovesChallenge() {
        neuralAiWinningMovesChallenge { ai, game, move ->
            if (ai.nextMove(game) == move.move) {
                1
            } else {
                0
            }
        }
    }

    @Test
    fun neuralAiWinningMovesRankedChallenge() {
        neuralAiWinningMovesChallenge { ai, game, move ->
            ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).map { (m, rating) ->
                if (rating > 0.5f && m == move.move) {
                    1
                } else {
                    0
                }
            }.sum()
        }
    }

    @Test
    fun neuralAiWinningMovesRankedChallengeStricter() {
        neuralAiWinningMovesChallenge { ai, game, move ->
            ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).map { (m, rating) ->
                if (rating > 0.8f && m == move.move) {
                    1
                } else {
                    0
                }
            }.sum()
        }
    }
}
