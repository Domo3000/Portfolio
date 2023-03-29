import connect4.ai.AI
import connect4.ai.length.*
import connect4.ai.monte.BalancedMonteCarloAI
import connect4.ai.monte.MaximizeWinsMonteCarloAI
import connect4.ai.monte.MinimizeLossesMonteCarloAI
import connect4.ai.neural.NeuralAI
import connect4.ai.simple.BiasedRandomAI
import connect4.ai.simple.RandomAI
import connect4.game.Connect4Game
import connect4.game.Player
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
    fun battleLengthTest() {
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
        println(measureTime {
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
        })
    }

    //  rough estimation of relative AI strength
    //      RandomAI: 115.0
    //      BiasedRandomAI: 115.0
    //      DumbLengthAI: 135.0
    //      AggressiveLengthAI: 175.0
    //      MaximizeWinsMonteCarloAI(100): 195.0
    //      MinimizeLossesMonteCarloAI(100): 235.0
    //      DefensiveLengthAI: 230.0
    //      SimpleLengthAI: 360.0
    //      MaximizeWinsMonteCarloAI(300): 375.0
    //      MinimizeLossesMonteCarloAI(500): 385.0
    //      MinimizeLossesMonteCarloAI(300): 410.0
    //      MaximizeWinsMonteCarloAI(500): 445.0
    //      PlyLengthAI: 610.0
    //      MinimizeLossesMonteCarloAI(800): 630.0
    //      MaximizeWinsMonteCarloAI(800): 660.0
    @Test
    fun relativeAiStrength() {
        //val storedHandler = StoredHandler()
        //storedHandler.loadStored(emptyList(), "s")
        //val aiScores = storedHandler.allNeurals().map { it to mutableListOf<Int>() }

        val aiScores = listOf(
            RandomAI(),
            BiasedRandomAI(),
            DumbLengthAI(),
            AggressiveLengthAI(),
            DefensiveLengthAI(),
            SimpleLengthAI(),
            PlyLengthAI()
        ).map { it to mutableListOf<Int>() }

        repeat(10) { round ->
            println(round)
            aiScores.forEach { (ai, scores) ->
                var winning = true
                var strength = 50

                while (winning) {
                    val monteCarloAI = BalancedMonteCarloAI(strength)
                    val p1Result = Connect4Game.runGame(ai, monteCarloAI)
                    val p2Result = Connect4Game.runGame(monteCarloAI, ai)

                    if (p1Result.first != Player.FirstPlayer && p2Result.first != Player.SecondPlayer) {
                        winning = false
                    }
                    strength += 50
                }
                scores += strength
            }
        }

        aiScores.map { (ai, scores) -> ai to scores.average() }.forEach { (ai, score) ->
            println("${ai.name}: $score")
        }
    }
}