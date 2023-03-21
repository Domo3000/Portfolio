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
            "neural" to handler.allNeurals().map { {it.ai} },
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
        val trainingPlayers = listOf(
            { SimpleLengthAI() },
            { BalancedLengthAI() },
            { BalancedLengthAI() },
            { DumbLengthAI() },
            { PlyLengthAI() },
            { PlyLengthAI() },
            { PlyLengthAI() },
            { MaximizeWinsMonteCarloAI(300) },
            { MaximizeWinsMonteCarloAI(500) },
            { MinimizeLossesMonteCarloAI(500) },
            { BalancedMonteCarloAI(700) },
            { BalancedMonteCarloAI(400) },
            { BalancedMonteCarloAI(1000) },
            { BalancedMonteCarloAI(200) }).map { it() }
        val battlePlayers = listOf(
            { SimpleLengthAI() },
            { PlyLengthAI() },
            { BalancedMonteCarloAI(400) },
            { BalancedMonteCarloAI(700) }
        )

        val moves = getTrainingMoves(trainingPlayers)

        handler.initWithBasicNeurals(moves)
        handler.initWithRandom(10, moves)

        println(moves.size)


        println("start training")
        handler.train(handler.allNeurals(), moves)
        repeat(10) {
            println(it)
            repeat(handler.allNeurals().size) {
                handler.battle(battlePlayers)
            }
        }
        println("initial battles done")
        handler.purge()
        handler.evolve()
        handler.resetBattles()

        repeat(100) { i ->
            println("outerLoop $i")
            handler.train(handler.allNeurals(), moves)
            repeat(10) { j ->
                println("innerLoop $j")
                repeat(handler.allNeurals().size) {
                    handler.battle(battlePlayers)
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
}