package trippy

import utils.mod

data class Node(val value: Int, val incoming: List<Int>, val outgoing: List<Int>)
// TODO test
class Graph(size: Int) {
    private fun new(size: Int) = (0 until size).map { n ->
        val (incoming, outgoing) = (1..(size / 2)).map { m -> ((n - m) mod size) to (n + m) % size }.unzip()
        Node(n, incoming, outgoing)
    }.toMutableList()

    val elements = new(size)

    fun get(n: Int) = elements.find { it.value == n }

    fun reset(size: Int) {
        elements.clear()
        elements.addAll(new(size))
    }
}