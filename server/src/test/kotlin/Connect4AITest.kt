import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.length.BalancedLengthAI
import connect4.ai.length.DumbLengthAI
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.*
import connect4.ai.simple.BiasedRandomAI
import connect4.game.Connect4Game
import connect4.game.Player
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private fun NeuralAI.nextMovePrint(field: List<List<Player?>>, availableColumns: List<Int>, player: Player) =
    nextMoveRanked(field, availableColumns, player).forEach { println(it) }

// TODO cleanup and implement some actual tests
@OptIn(ExperimentalTime::class)
class Connect4AITest {
    private fun battle(games: Int, players: List<AI>, log: Boolean) {
        val resultMap = mutableMapOf<Pair<String, String>, Int>()
        players.forEach { a ->
            players.forEach { b ->
                resultMap[a.name to b.name] = 0
            }
        }

        repeat(games) {
            players.forEach { p1 ->
                players.forEach { p2 ->
                    val result = Connect4Game.runGame(p1, p2)

                    when (result.first) {
                        Player.FirstPlayer -> {
                            resultMap[p1.name to p2.name] = resultMap[p1.name to p2.name]!! + 1
                        }

                        Player.SecondPlayer -> {
                            resultMap[p2.name to p1.name] = resultMap[p2.name to p1.name]!! + 1
                        }

                        else -> {}
                    }
                }
            }
        }

        if (log) {
            resultMap.forEach { (pNr, wins) -> println("$pNr: $wins") }
        }

        players.map { it.name }.map { index ->
            index to resultMap.filter { index == it.key.first }.values.sum()
        }.sortedBy { it.second }.forEach {
            println(it)
        }
    }

    @Test
    fun lengthAITest() {
        val x = measureTime {
            battle(
                1,
                listOf(
                    BalancedMonteCarloAI(500),
                    MaximizeWinsMonteCarloAI(501),
                    MinimizeLossesMonteCarloAI(502),
                    DumbLengthAI(),
                    SimpleLengthAI(),
                    PlyLengthAI()
                ),
                true
            )
        }
        println(x)
    }

    @Test
    fun neuralTest() {
        val handler = EvolutionHandler()

        val toEvaluate = listOf(
            "simple" to AIs.simpleAIs,
            "medium" to AIs.mediumAIs,
            "high" to AIs.highAIs
        )

        toEvaluate.forEach { (aiName, ais) ->
            println(aiName)
            repeat(10) {
                println(it)
                repeat(handler.allNeurals().size) {
                    handler.battle(ais)
                }
            }
            handler.currentScore()
            handler.resetBattles()
        }
    }

    // let this run to train and store a lot of neurals
    // use neuralTest to evaluate them later
    @Test
    fun purgeEvolve() {
        val handler = EvolutionHandler()
        val trainingPlayers = listOf({ BiasedRandomAI() },
            { SimpleLengthAI() },
            { BalancedLengthAI() },
            { BalancedLengthAI() },
            { DumbLengthAI() },
            { DumbLengthAI() },
            { PlyLengthAI() },
            { PlyLengthAI() },
            { PlyLengthAI() },
            { MaximizeWinsMonteCarloAI(300) },
            { MaximizeWinsMonteCarloAI(500) },
            { MinimizeLossesMonteCarloAI(500) },
            { MinimizeLossesMonteCarloAI(300) },
            { BalancedMonteCarloAI(700) },
            { BalancedMonteCarloAI(400) },
            { BalancedMonteCarloAI(1000) },
            { BalancedMonteCarloAI(200) }).map { it() }

        val moves = getTrainingMoves(trainingPlayers)

        handler.initWithBasicNeurals(moves)
        handler.initWithRandom(10, moves)

        println(moves.size)

        println("start training")
        handler.train(handler.allNeurals(), moves)
        repeat(10) {
            println(it)
            repeat(handler.allNeurals().size) {
                handler.battle(AIs.mediumAIs)
            }
        }
        println("initial battles done")
        handler.purge(30)
        handler.evolve()
        handler.resetBattles()

        repeat(100) { i ->
            println("outerLoop $i")
            handler.train(handler.allNeurals(), moves)
            repeat(10) { j ->
                println("innerLoop $j")
                repeat(handler.allNeurals().size) {
                    handler.battle(listOf(
                        { PlyLengthAI() },
                        { SimpleLengthAI() },
                        { BalancedMonteCarloAI(400) }
                    ))
                }
            }
            handler.currentScore(true, true)
            handler.storeHighest(5)
            handler.highestRanking(3).forEach { counter ->
                handler.storeStrongest(counter.ai)
            }
            handler.purge()
            handler.evolve()
            handler.resetBattles()
        }
    }

    private fun printChallenge(ai: RandomNeuralAI) {
        val game = Connect4Game()

        println(game)
        ai.nextMovePrint(game.field, game.availableColumns, game.currentPlayer)

        game.makeMove(4)
        game.makeMove(5)
        game.makeMove(4)
        game.makeMove(5)
        game.makeMove(4)

        println(game)
        ai.nextMovePrint(game.field, game.availableColumns, game.currentPlayer)

        game.makeMove(4)
        game.makeMove(2)
        game.makeMove(2)
        game.makeMove(2)
        game.makeMove(2)
        game.makeMove(1)
        game.makeMove(1)
        game.makeMove(1)
        game.makeMove(1)

        println(game)
        ai.nextMovePrint(game.field, game.availableColumns, game.currentPlayer)

        game.makeMove(2)
        game.makeMove(2)
        game.makeMove(1)
        game.makeMove(1)

        println(game)
        ai.nextMovePrint(game.field, game.availableColumns, game.currentPlayer)

        game.makeMove(6)
        game.makeMove(5)
        game.makeMove(4)
        game.makeMove(6)
        game.makeMove(6)
        game.makeMove(3)
        game.makeMove(3)
        game.makeMove(0)
        game.makeMove(0)
        game.makeMove(3)
        game.makeMove(3)
        game.makeMove(3)

        println(game)
        ai.nextMovePrint(game.field, game.availableColumns, game.currentPlayer)

        val game2 = Connect4Game()

        game2.makeMove(5)
        game2.makeMove(6)
        game2.makeMove(5)
        game2.makeMove(6)
        game2.makeMove(5)

        println(game2)
        ai.nextMovePrint(game2.field, game2.availableColumns, game2.currentPlayer)

        val game3 = Connect4Game()

        game3.makeMove(1)
        game3.makeMove(1)
        game3.makeMove(2)
        game3.makeMove(1)
        game3.makeMove(3)

        println(game3)
        ai.nextMovePrint(game3.field, game3.availableColumns, game3.currentPlayer)

        game3.makeMove(4)
        game3.makeMove(2)
        game3.makeMove(2)
        game3.makeMove(5)
        game3.makeMove(3)
        game3.makeMove(5)

        println(game3)
        ai.nextMovePrint(game3.field, game3.availableColumns, game3.currentPlayer)
    }

    @Test
    fun neuralChallenge() {
        val handler = EvolutionHandler()
        var maxCorrect = 5
        var highest: NeuralCounter? = null
        var prevHighest: NeuralCounter? = null
        var prev2Highest: NeuralCounter? = null
        var maxPoints = 75
        var mostPoints: NeuralCounter? = null
        var prevmostPoints: NeuralCounter? = null
        var prev2mostPoints: NeuralCounter? = null

        handler.allNeurals().forEach { counter ->
            var correct = 0
            val game = Connect4Game()
            val ai = counter.ai

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 3) {
                correct += 1
            }

            game.makeMove(4)
            game.makeMove(5)
            game.makeMove(4)
            game.makeMove(5)
            game.makeMove(4)

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 4) {
                correct += 3
            } else if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                    .first().first == 5
            ) {
                correct += 1
            } else {
                correct -= 1
            }

            game.makeMove(4)
            game.makeMove(2)
            game.makeMove(2)
            game.makeMove(2)
            game.makeMove(2)
            game.makeMove(1)
            game.makeMove(1)
            game.makeMove(1)
            game.makeMove(1)

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 0) {
                correct += 3
            } else if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                    .first().first == 3
            ) {
                correct += 1
            } else {
                correct -= 1
            }

            game.makeMove(2)
            game.makeMove(2)
            game.makeMove(1)
            game.makeMove(1)

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                    .first().first == 3 || ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                    .first().first == 0
            ) {
                correct++
            }

            game.makeMove(6)
            game.makeMove(5)
            game.makeMove(4)
            game.makeMove(6)
            game.makeMove(6)
            game.makeMove(3)
            game.makeMove(3)
            game.makeMove(0)
            game.makeMove(0)
            game.makeMove(3)
            game.makeMove(3)
            game.makeMove(3)

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 5
            ) {
                correct += 3
            }

            val game2 = Connect4Game()

            game2.makeMove(5)
            game2.makeMove(6)
            game2.makeMove(5)
            game2.makeMove(6)
            game2.makeMove(5)

            if (ai.nextMoveRanked(game2.field, game2.availableColumns, game2.currentPlayer)
                    .first().first == 5 || ai.nextMoveRanked(
                    game2.field,
                    game2.availableColumns,
                    game2.currentPlayer
                )
                    .first().first == 6
            ) {
                correct += 2
            }

            val game3 = Connect4Game()

            game3.makeMove(1)
            game3.makeMove(1)
            game3.makeMove(2)
            game3.makeMove(1)
            game3.makeMove(3)

            if (ai.nextMoveRanked(game3.field, game3.availableColumns, game3.currentPlayer)
                    .first().first == 0 || ai.nextMoveRanked(
                    game3.field,
                    game3.availableColumns,
                    game3.currentPlayer
                )
                    .first().first == 4
            ) {
                correct += 2
            }

            game3.makeMove(4)
            game3.makeMove(2)
            game3.makeMove(2)
            game3.makeMove(5)
            game3.makeMove(3)
            game3.makeMove(5)

            if (ai.nextMoveRanked(game3.field, game3.availableColumns, game3.currentPlayer)
                    .first().first == 1
            ) {
                correct += 2
            } else {
                correct -= 1
            }

            if (correct >= maxCorrect) {
                println("newHighest: $correct")
                println(counter.ai.info())
                maxCorrect = correct
                prev2Highest = prevHighest
                prevHighest = highest
                highest = counter

                printChallenge(counter.ai)
            } else {
                println(counter.ai.info())
                println(correct)
            }
            counter.gamesWon = 0
            repeat(5) { handler.battle(AIs.simpleAIs, counter) }
            val beforeMedium = counter.gamesWon
            repeat(5) { handler.battle(AIs.mediumAIs, counter) }
            val beforeHigh = counter.gamesWon
            repeat(5) { handler.battle(AIs.highAIs, counter) }
            val beforePly = counter.gamesWon
            repeat(15) { handler.battle(listOf { PlyLengthAI() }, counter) }
            val afterPly = counter.gamesWon

            val score =
                beforeMedium + ((beforeHigh - beforeMedium) * 2) + ((beforePly - beforeHigh) * 4) + ((afterPly - beforePly) * 8)

            if (score >= maxPoints) {
                println("new Highscore: $score")
                println(beforeMedium)
                println((beforeHigh - beforeMedium))
                println((beforePly - beforeHigh))
                println((afterPly - beforePly))
                println(counter.ai.info())

                Connect4Game.runGame(counter.ai, BiasedRandomAI(), true, false)
                Connect4Game.runGame(BiasedRandomAI(), counter.ai, true, false)
                Connect4Game.runGame(counter.ai, PlyLengthAI(), true, false)
                Connect4Game.runGame(PlyLengthAI(), counter.ai, true, false)

                maxPoints = score
                prev2mostPoints = prevmostPoints
                prevmostPoints = mostPoints
                mostPoints = counter
            } else {
                println(score)
            }
        }

        listOfNotNull(
            highest,
            prevHighest,
            prev2Highest,
            mostPoints,
            prevmostPoints,
            prev2mostPoints
        ).forEach { counter ->
            println(counter.ai.info())
            //handler.storeStrongest(counter.ai)
        }
    }
}