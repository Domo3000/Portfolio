import trippy.Graph
import utils.mod
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

typealias Position = Pair<Int, Int>

class LookupMap(size: Int) {
    private val graph = Graph(size)
    private val map: HashMap<Pair<Int, Int>, Boolean> = hashMapOf()

    fun fight(defender: Int, fighter: Int) = map.getOrPut(defender to fighter) {
        graph.get(defender)!!.incoming.contains(fighter)
    }
}

/*
    true if fighter wins
 */
fun calcFight(defender: Int, fighter: Int, size: Int): Boolean = if (defender == fighter) {
    false
} else if (defender > fighter) {
    defender - fighter <= size / 2
} else {
    size - fighter + defender <= size / 2
}

interface RunStep {
    fun runStep()
}

fun <T> Array<T>.toArrayList(): ArrayList<T> = ArrayList(this.toList())

abstract class ArrayListWrappingArray(val sizeX: Int, val sizeY: Int, val states: Int) {
    val threshold = 1
    val randomMod = 2
    val random = Random(0)

    val elements: ArrayList<ArrayList<Int>> = Array(sizeY) {
        Array(sizeX) {
            random.nextInt() mod states
        }.toArrayList()
    }.toArrayList()

    fun get(x: Int, y: Int): Int = elements[y mod sizeY][x mod sizeX]

    fun set(x: Int, y: Int, value: Int) {
        elements[y][x] = value
    }

    private val withPosition
        get() = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element -> Position(x, y) to element }
        }.flatten()

    abstract fun fight(defender: Int, fighter: Int): Boolean

    open fun runStep() {
        withPosition.map { (position, node) ->
            val incoming = (-1..1).map { offsetY ->
                (-1..1).mapNotNull { offsetX ->
                    if (offsetX == 0 && offsetY == 0) {
                        null
                    } else {
                        get(position.first + offsetX, position.second + offsetY)
                    }
                }
            }.flatten().filter { fight(node, it) }

            position to incoming
        }.forEach { (position, incoming) ->
            incoming.distinct().map { n -> n to incoming.count { n == it } }.shuffled().maxByOrNull { it.second }
                ?.let { (n, count) ->
                    val r = if (randomMod == 1) {
                        0
                    } else {
                        (random.nextInt() mod randomMod)
                    }

                    if (count > threshold + r) {
                        set(position.first, position.second, n)
                    }
                }
        }
    }
}

abstract class MutableListWrappingArray(val sizeX: Int, val sizeY: Int, val states: Int) {
    val threshold = 1
    val randomMod = 2
    val random = Random(0)

    val elements: MutableList<MutableList<Int>> = Array(sizeY) {
        Array(sizeX) {
            random.nextInt() mod states
        }.toMutableList()
    }.toMutableList()

    fun get(x: Int, y: Int): Int = elements[y mod sizeY][x mod sizeX]

    fun set(x: Int, y: Int, value: Int) {
        elements[y][x] = value
    }

    open fun runStep() {
        val next: List<List<Int>> = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element ->
                val incoming = (-1..1).map { offsetY ->
                    (-1..1).mapNotNull { offsetX ->
                        if (offsetX == 0 && offsetY == 0) {
                            null
                        } else {
                            get(x + offsetX, y + offsetY)
                        }
                    }
                }.flatten()

                val grouping = incoming.filter { calcFight(element, it, states) }.groupingBy { it }.eachCount()
                grouping.maxByOrNull { it.value }?.let { entry ->
                    val count = entry.value

                    val r = if (randomMod == 1) {
                        0
                    } else {
                        (random.nextInt() mod randomMod)
                    }

                    if (count > threshold + r) {
                        grouping.filter { it.value == count }.keys.random()
                    } else {
                        null
                    }
                } ?: element
            }
        }
        elements.clear()
        elements.addAll(next.map { it.toMutableList() }.toMutableList())
    }
}

class MapArray(x: Int, y: Int, s: Int) : ArrayListWrappingArray(x, y, s), RunStep {
    private var lookupMap = LookupMap(states)

    override fun fight(defender: Int, fighter: Int): Boolean = lookupMap.fight(defender, fighter)
}

class CalcArray(x: Int, y: Int, s: Int) : ArrayListWrappingArray(x, y, s), RunStep {
    override fun fight(defender: Int, fighter: Int): Boolean = calcFight(defender, fighter, states)
}

class ImprovedMutableArray(x: Int, y: Int, s: Int) : MutableListWrappingArray(x, y, s), RunStep

class ImprovedSetArray(x: Int, y: Int, s: Int) : MutableListWrappingArray(x, y, s), RunStep {
    override fun runStep() {
        elements.mapIndexed { y, list ->
            list.mapIndexed { x, element ->
                val incoming = (-1..1).map { offsetY ->
                    (-1..1).mapNotNull { offsetX ->
                        if (offsetX == 0 && offsetY == 0) {
                            null
                        } else {
                            get(x + offsetX, y + offsetY)
                        }
                    }
                }.flatten()

                val grouping = incoming.filter { calcFight(element, it, states) }.groupingBy { it }.eachCount()
                grouping.maxByOrNull { it.value }?.let { entry ->
                    val count = entry.value

                    val r = if (randomMod == 1) {
                        0
                    } else {
                        (random.nextInt() mod randomMod)
                    }

                    if (count > threshold + r) {
                        Position(x, y) to grouping.filter { it.value == count }.keys.random()
                    } else {
                        null
                    }
                } ?: (Position(x, y) to element)
            }
        }.flatten().forEach { (position, value) ->
            set(position.first, position.second, value)
        }
    }
}

class ImprovedArray(x: Int, y: Int, s: Int) : ArrayListWrappingArray(x, y, s), RunStep {
    override fun fight(defender: Int, fighter: Int): Boolean = calcFight(defender, fighter, states)

    override fun runStep() {
        val next: List<List<Int>> = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element ->
                val incoming = (-1..1).map { offsetY ->
                    (-1..1).mapNotNull { offsetX ->
                        if (offsetX == 0 && offsetY == 0) {
                            null
                        } else {
                            get(x + offsetX, y + offsetY)
                        }
                    }
                }.flatten().filter { fight(element, it) }

                val grouping = incoming.groupingBy { it }.eachCount()
                grouping.maxByOrNull { it.value }?.let { entry ->
                    val count = entry.value

                    val r = if (randomMod == 1) {
                        0
                    } else {
                        (random.nextInt() mod randomMod)
                    }

                    if (count > threshold + r) {
                        grouping.filter { it.value == count }.keys.random()
                    } else {
                        null
                    }
                } ?: element
            }
        }
        elements.clear()
        elements.addAll(next.map { ArrayList(it) })
    }
}

class NoGroupingImprovedArray(x: Int, y: Int, s: Int) : ArrayListWrappingArray(x, y, s), RunStep {
    override fun fight(defender: Int, fighter: Int): Boolean = calcFight(defender, fighter, states)

    override fun runStep() {
        val next: List<List<Int>> = elements.mapIndexed { y, list ->
            list.mapIndexed { x, element ->
                val incoming = (-1..1).map { offsetY ->
                    (-1..1).mapNotNull { offsetX ->
                        if (offsetX == 0 && offsetY == 0) {
                            null
                        } else {
                            get(x + offsetX, y + offsetY)
                        }
                    }
                }.flatten().filter { fight(element, it) }

                incoming.distinct().map { n -> n to incoming.count { n == it } }.shuffled()
                    .maxByOrNull { it.second }
                    ?.let { entry ->
                        val count = entry.second

                        val r = if (randomMod == 1) {
                            0
                        } else {
                            (random.nextInt() mod randomMod)
                        }

                        if (count > threshold + r) {
                            entry.first
                        } else {
                            null
                        }
                    } ?: element
            }
        }
        elements.clear()
        elements.addAll(next.map { ArrayList(it) })
    }
}

class TrippyBenchmarks {
    @Test
    fun fightTest() {
        (1..12).forEach { s ->
            val size = s * 2 + 1
            val map = LookupMap(size)
            (0 until size).forEach { defender ->
                (0 until size).forEach { fighter ->
                    assertEquals(
                        map.fight(defender, fighter), calcFight(defender, fighter, size)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun fightBenchmark() {
        val mapTime = "mapTime" to measureTime {
            (1..12).forEach { s ->
                val size = s * 2 + 1
                val map = LookupMap(size)
                repeat(1000) {
                    (0 until size).forEach { defender ->
                        (0 until size).forEach { fighter ->
                            map.fight(defender, fighter)
                        }
                    }
                }
            }
        }

        val calcTime = "calcTime" to measureTime {
            (1..12).forEach { s ->
                val size = s * 2 + 1
                repeat(1000) {
                    (0 until size).forEach { defender ->
                        (0 until size).forEach { fighter ->
                            calcFight(defender, fighter, size)
                        }
                    }
                }
            }
        }

        console.log("$mapTime vs $calcTime")
    }

    /*
     * MutableList vs ArrayList makes a negligible difference
     *
     * The following makes a bigger difference:
     * incoming.distinct().map { n -> n to incoming.count { n == it } }.shuffled().maxByOrNull { it.second }
     * incoming.groupingBy { it }.eachCount().grouping.maxByOrNull { it.value }
     * with the grouping being 10% faster
     *
     * LookupMap for fights is much slower
     *
     * Across various tests ImprovedMutableArray has been the fastest, nearly twice as fast as the original MapArray
     */
    @OptIn(ExperimentalTime::class)
    @Test
    fun arrayBenchmark() {
        val dimension = 30
        val repeat = 3

        val list =
            listOf<Pair<String, (Int) -> RunStep>>(
                "MapArray" to { size -> MapArray(dimension, dimension, size) },
                "CalcArray" to { size -> CalcArray(dimension, dimension, size) },
                "ImprovedArray" to { size -> ImprovedArray(dimension, dimension, size) },
                "ImprovedMutableArray" to { size -> ImprovedMutableArray(dimension, dimension, size) },
                "NoGroupingImprovedArray" to { size -> NoGroupingImprovedArray(dimension, dimension, size) },
                "ImprovedSetArray" to { size -> ImprovedSetArray(dimension, dimension, size) }
            )

        list.map { (name, constructor) ->
            val time = measureTime {
                (1..12).forEach { s ->
                    val size = s * 2 + 1
                    val array = constructor(size)
                    repeat(repeat) {
                        array.runStep()
                    }
                }
            }
            name to time
        }.sortedBy { it.second }.map { (name, time) ->
            console.log("$name: $time")
        }
    }
}