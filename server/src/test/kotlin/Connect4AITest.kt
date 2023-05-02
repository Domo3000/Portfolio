import connect4.ai.AI
import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.NeuralAI
import connect4.ai.neural.StoredHandler
import connect4.ai.simple.BiasedRandomAI
import connect4.ai.simple.RandomAI
import connect4.game.Connect4Game
import connect4.game.Player
import kotlinx.coroutines.*
import org.junit.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private fun NeuralAI.nextMovePrint(field: List<List<Player?>>, availableColumns: List<Int>, player: Player) =
    nextMoveRanked(field, availableColumns, player).forEach { println(it) }

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
    fun battleLengthTestLength() {
        println(measureTime {
            battle(
                100,
                listOf(
                    SimpleLengthAI(),
                    PlyLengthAI()
                ),
                false
            )
        })
    }

    @Test
    fun battleLengthTestMonte() {
        println(measureTime {
            battle(
                1,
                listOf(
                    BalancedMonteCarloAI(1000),
                    PlyLengthAI()
                ),
                true
            )
        })
    }

    private fun evaluateSingle(ai: AI): Int {
        var winning = true
        var strength = 50

        while (winning) {
            val p1Result = Connect4Game.runGame(ai, BalancedMonteCarloAI(strength))
            val p2Result = Connect4Game.runGame(BalancedMonteCarloAI(strength), ai)

            if (p1Result.first != Player.FirstPlayer && p2Result.first != Player.SecondPlayer) {
                winning = false
            }
            strength += 50
        }

        return strength
    }

    //  rough estimation of relative AI strength
    private fun evaluateRelativeAiStrength(ais: List<AI>, repeat: Int = 10) {
        val aiScores = ais.map { it to mutableListOf<Int>() }

        repeat(repeat) { round ->
            println(round)
            runBlocking {
                aiScores.map { (ai, scores) ->
                    CoroutineScope(Dispatchers.Default).async {
                        scores += evaluateSingle(ai)
                    }
                }.awaitAll()
            }
        }

        aiScores.map { (ai, scores) -> ai to scores.average() }.sortedBy { it.second }.forEach { (ai, score) ->
            println("${ai.name}: $score")
        }
    }

    private fun evaluateRelativeAiStrength(ai: () -> AI, repeat: Int = 15): Int {
        val aiScore = mutableListOf<Int>()

        runBlocking {
            (0 until repeat).map { round ->
                CoroutineScope(Dispatchers.Default).async {
                    val score = evaluateSingle(ai())
                    aiScore += score
                }
            }.awaitAll()
        }

        val result = aiScore.average()
        return result.toInt()
    }

    //      RandomAI: 125.0
    //      BiasedRandomAI: 145.0
    //      DumbLengthAI: 145.0
    //      DefensiveLengthAI: 170.0
    //      AggressiveLengthAI: 210.0
    //      MaximizeWinsMonteCarloAI(300): 210.0
    //      DefensiveLengthAI: 230.0
    //      MinimizeLossesMonteCarloAI(300): 280.0
    //      MonteCarlo(300): 295.0 - 385.0
    //      BalancedLengthAI: 315.0
    //      SimpleLengthAI: 320.0
    //      PlyLengthAI: 695.0
    //      MonteCarlo(800): 550.0 - 660.0 (should be higher?)
    @Test
    fun relativeAiStrength() {
        val ais = listOf(
            RandomAI(),
            BiasedRandomAI(),
            DumbLengthAI(),
            DefensiveLengthAI(),
            AggressiveLengthAI(),
            SimpleLengthAI(),
            BalancedLengthAI(),
            PlyLengthAI(),
            MaximizeWinsMonteCarloAI(300),
            MinimizeLossesMonteCarloAI(300),
            BalancedMonteCarloAI(300),
            BalancedMonteCarloAI(800)
        )

        ais.map { ai ->
            val result = evaluateRelativeAiStrength({ ai })
            ai to result
        }.sortedBy { it.second }.forEach {
            println("${it.first.name}: ${it.second}")
        }
    }

    @Test
    fun relativeNeuralAiStrength() {
        val storedHandler = StoredHandler()
        storedHandler.loadStored(silent = true)

        val allCombinations = CombinationCreator.fromNeurals(storedHandler.allNeurals())
        println(allCombinations.size)

        allCombinations.shuffled().mapIndexed { i, ai ->
            println(i)
            val result = evaluateRelativeAiStrength({ ai })
            println("${ai.name}: $result")
            ai to result
        }.sortedBy { it.second }.forEach {
            println("${it.first.name}: ${it.second}")
        }
    }
}