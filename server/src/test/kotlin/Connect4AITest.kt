import connect4.ai.AI
import connect4.ai.AIs
import connect4.ai.length.PlyLengthAI
import connect4.ai.length.SimpleLengthAI
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.EvolutionHandler
import connect4.ai.neural.NeuralAI
import connect4.ai.neural.NeuralCounter
import connect4.ai.neural.RandomNeuralAI
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
    private fun battle(games: Int, players: List<AI> = emptyList(), log: Boolean = false) {
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
                    BalancedMonteCarloAI(1001),
                    MaximizeWinsMonteCarloAI(1000),
                    MinimizeLossesMonteCarloAI(999),
                    SimpleLengthAI(),
                    PlyLengthAI()
                ),
                true
            )
        }
        println(x)
        battle(
            10,
            listOf(
                BalancedMonteCarloAI(1001),
                MaximizeWinsMonteCarloAI(1000),
                MinimizeLossesMonteCarloAI(999),
                SimpleLengthAI(),
                PlyLengthAI()
            ),
            true
        )
        //battle(20, listOf(BiasedRandomAI(), BalancedMonteCarloAI(501), MaximizeWinsMonteCarloAI(500), MinimizeLossesMonteCarloAI(499), SimpleLengthAI(), PlyLengthAI(), BalancedLengthAI(), DumbLengthAI()), true)
        //battle(20, listOf(BiasedRandomAI(), MonteCarloAI(10), MonteCarloAI(50), MonteCarloAI(100), MonteCarloAI(200), MonteCarloAI(500), SimpleLengthAI(), PlyLengthAI(), BalancedLengthAI(), DumbLengthAI()))
    }

    @Test
    fun neuralTest() {
        val handler = EvolutionHandler(1)

        val toEvaluate = listOf(
            //"simple" to AIs.simpleAIs,
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
            handler.resetBattles(true, true)
        }
    }

    @Test
    fun neuralTrainingTest() {
        val handler = EvolutionHandler(1)
        handler.evaluate()
        repeat(5) {
            println(it)
            repeat(handler.allNeurals().size) {
                handler.battle()
            }
        }
        repeat(3) {
            println(it)

            handler.train(handler.allNeurals())
        }

        repeat(1000) {
            repeat(5) {
                println(it)
                repeat(handler.allNeurals().size) {
                    handler.battle()
                }
                handler.train(handler.allNeurals())
            }
            handler.storeHighest(3)

            if (handler.evaluate()) {
                val game = Connect4Game()
                val ai = handler.strongest!!.ai

                println(game)

                ai.nextMovePrint(game.field, game.availableColumns, game.currentPlayer)

                game.makeMove(3)
                game.makeMove(4)
                game.makeMove(3)
                game.makeMove(4)
                game.makeMove(3)

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

                game.makeMove(0)
                game.makeMove(6)
                game.makeMove(5)
                game.makeMove(5)

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
            }

            if (it % 20 == 0) {
                handler.evolve()
            }
        }
    }

    @Test
    fun neuralChallenge() {
        val handler = EvolutionHandler(1)
        var maxCorrect = 4

        //repeat(100) {
        handler.allNeurals().forEach { counter ->
            var correct = 0
            val game = Connect4Game()
            val ai = counter.ai

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 3) {
                correct++
            }

            game.makeMove(4)
            game.makeMove(5)
            game.makeMove(4)
            game.makeMove(5)
            game.makeMove(4)

            if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 4) {
                correct += 2
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

            val move0 = ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                .first().first

            if (move0 == 3 || move0 == 0) {
                correct++
            }

            game.makeMove(2)
            game.makeMove(2)
            game.makeMove(1)
            game.makeMove(1)

            val move1 = ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer)
                .first().first

            if (move1 == 3 || move1 == 0) {
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

            val move2 = ai.nextMoveRanked(game2.field, game2.availableColumns, game2.currentPlayer)
                .first().first

            if (move2 == 5 || move2 == 6) {
                correct++
            }

            val game3 = Connect4Game()

            game3.makeMove(1)
            game3.makeMove(1)
            game3.makeMove(2)
            game3.makeMove(1)
            game3.makeMove(3)

            val move3 = ai.nextMoveRanked(game3.field, game3.availableColumns, game3.currentPlayer)
                .first().first

            if (move3 == 0 || move3 == 4) {
                correct += 2
            }

            if (correct >= maxCorrect) {
                println("newHighest: $correct")
                println(counter.ai.info())
                maxCorrect = correct
                //handler.storeStrongest(ai)
            }
        }
        /*
            repeat(handler.allNeurals().size * 3) {
                handler.battle()
                handler.train(handler.allNeurals())
            }
            handler.evaluate()

         */
        //}
    }

    @Test
    fun monteCarloTest() {
        /*
        val simpleLengthAI = PlyLengthAI()

        val monte10 = MonteCarloAI(10)
        val monte50 = MonteCarloAI(50)
        val monte100 = MonteCarloAI(100)
        val monte200 = MonteCarloAI(200)

        var count10 = 0 to 0
        var count50 = 0 to 0
        var count100 = 0 to 0
        var count200 = 0 to 0
        var count = 1

        var oldCount10 = 0 to 0
        var oldCount50 = 0 to 0
        var oldCount100 = 0 to 0
        var oldCount200 = 0 to 0

        repeat(2000) {
            if (it % 10 == 0 || it == 1) {
                println(monte100.parentNode.haveFinished())
                println(monte200.parentNode.haveFinished())
                println("count10: ${count10.first / count} (${(count10.first - oldCount10.first)}) ${count10.second} (${count10.second - oldCount10.second})")
                println("count50: ${count50.first / count} (${(count50.first - oldCount50.first)}) ${count50.second} (${count50.second - oldCount50.second})")
                println("count100: ${count100.first / count} (${(count100.first - oldCount100.first)}) ${count100.second} (${count100.second - oldCount100.second})")
                println("count200: ${count200.first / count} (${(count200.first - oldCount200.first)}) ${count200.second} (${count200.second - oldCount200.second})")
                oldCount10 = count10
                oldCount50 = count50
                oldCount100 = count100
                oldCount200 = count200
            }

            val time10 = measureTime {
                if (Connect4Game.runGame(monte10, simpleLengthAI).first == Player.FirstPlayer) {
                    count10 = count10.first to (count10.second + 1)
                }
            }

            count10 = (count10.first + time10.toInt(DurationUnit.MILLISECONDS)) to count10.second

            val time50 = measureTime {
                if (Connect4Game.runGame(monte50, simpleLengthAI).first == Player.FirstPlayer) {
                    count50 = count50.first to (count50.second + 1)
                }
            }

            count50 = (count50.first + time50.toInt(DurationUnit.MILLISECONDS)) to count50.second

            val time100 = measureTime {
                if (Connect4Game.runGame(monte100, simpleLengthAI).first == Player.FirstPlayer) {
                    count100 = count100.first to (count100.second + 1)
                }
            }

            count100 = (count100.first + time100.toInt(DurationUnit.MILLISECONDS)) to count100.second

            val time200 = measureTime {
                if (Connect4Game.runGame(monte200, simpleLengthAI).first == Player.FirstPlayer) {
                    count200 = count200.first to (count200.second + 1)
                }
            }

            count200 = (count200.first + time200.toInt(DurationUnit.MILLISECONDS)) to count200.second

            count++
        }
        println("count10: ${count10.first / count} ${count10.second}")
        println("count50: ${count50.first / count} ${count50.second}")
        println("count100: ${count100.first / count} ${count100.second}")
        println("count200: ${count200.first / count} ${count200.second}")

         */
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
    fun neuralChallenge2() {
        val handler = EvolutionHandler(1)
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
            } else if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 5) {
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
            } else if (ai.nextMoveRanked(game.field, game.availableColumns, game.currentPlayer).first().first == 3) {
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
                    .first().first == 5 || ai.nextMoveRanked(game2.field, game2.availableColumns, game2.currentPlayer)
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
                    .first().first == 0 || ai.nextMoveRanked(game3.field, game3.availableColumns, game3.currentPlayer)
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


    /*
    @Test
    fun simpleBattle() {
        battle(5, AIs.simpleAIs, true)
    }

    @Test
    fun mediumBattle() {
        battle(5, AIs.mediumAIs, true)
    }

    @Test
    fun highBattle() {
        battle(5, AIs.highAIs, true)
    }

    @Test
    fun allBattle() {
        println( measureTime { battle(1, AIs.allNonNeurals, true) })
    }

    @Test
    fun evolveTest() {
        val evolutionHandler = EvolutionHandler()

        println(measureTime { evolutionHandler.train() })
        println(measureTime { evolutionHandler.battle(1, false, players = AIs.simpleAIs) })
        println(measureTime { evolutionHandler.battle(1, false, players = AIs.mediumAIs) })
        println(measureTime { evolutionHandler.battle(1, false, players = AIs.highAIs) })
        println(measureTime { evolutionHandler.train() })
        println(measureTime { evolutionHandler.tinyBattle(true) })

        repeat(2000) { i ->
            println()
            println()
            println(i)
            evolutionHandler.battle(1, false)
            evolutionHandler.train()
            evolutionHandler.train()
            evolutionHandler.train()
            evolutionHandler.battle(1, false)
            evolutionHandler.train()
            println(measureTime { evolutionHandler.train() })
            evolutionHandler.battle(5, true)
            evolutionHandler.tinyBattle(true)
            //evolutionHandler.storeHighest(3)
            evolutionHandler.evolve()
        }
    }

    @Test
    fun runGame() {
        val monti = MonteCarloAI(350)
        println( measureTime { monti.parentNode.haveFinished() })
        println( monti.parentNode.haveFinished())
        println( measureTime { Connect4Game.runGame(monti, PlyLengthAI(), true, true) })
        println( measureTime { monti.parentNode.haveFinished() })
        println( monti.parentNode.haveFinished())
        repeat(100) {
            println( measureTime { Connect4Game.runGame(monti, PlyLengthAI(), true, false) })
            println( measureTime { monti.parentNode.haveFinished() })
            println( measureTime { monti.parentNode.isFinished() })
            println( monti.parentNode.haveFinished())
        }
    }

     */
}