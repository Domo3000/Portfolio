package about

import Deck
import Result
import react.StateSetter

data class ShuffleCounter(val deck: Deck, var counter: Long = 0, var finished: Boolean = false)

data class Position(val x: Int = -1, val y: Int = -1) {
    override fun equals(other: Any?): Boolean {
        (other as? Position)?.let {
            if (x != it.x) return false
            if (y != it.y) return false
        } ?: return false

        return true
    }
}

fun Position.toPrettyString() = "${this.y + 2}:${this.x + 2}"

class State(initialSize: Int, private val outShuffle: Boolean, private val setSize: StateSetter<Int>) {
    var size = initialSize

    val decks = Array(size) { y ->
        Array(y + 1) { x ->
            Position(x, y) to ShuffleCounter(Deck(y + 2, outShuffle))
        }.toList()
    }.toMutableList()

    val unfinished
        get() = decks.flatten().filter { !it.second.finished }

    val unfinishedCount
        get() = unfinished.count()

    val finished
        get() = decks.flatten().filter { it.second.finished }

    val finishedCount
        get() = finished.count()

    fun addRow() {
        size += 1
        val newRow = Array(size) { x -> Position(x, size - 1) to ShuffleCounter(Deck(size + 1, outShuffle)) }.toList()
        decks.add(newRow)
        setSize(size)
    }

    fun loadFromResult(result: Result) {
        val newRows = mutableListOf(
            listOf(Position(0, 0) to ShuffleCounter(Deck(1, outShuffle), 1, true))
        )

        result.rows.map { row ->
            val y = row.size
            newRows.add(row.counters.map { counter ->
                val x = counter.piles
                Position(x - 2, y - 2) to ShuffleCounter(Deck(1, outShuffle), counter.count, true)
            } + (Position(y - 2, y - 2) to ShuffleCounter(Deck(1, outShuffle), 1, true)))
        }


        decks.clear()
        decks.addAll(newRows)
        size = newRows.size
        setSize(newRows.size)
    }
}