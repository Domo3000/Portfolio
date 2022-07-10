package structures

import kotlin.test.Test
import kotlin.test.assertEquals

class KdTreeTest {

    @Test
    fun singleNode() {
        val node = Node(RelativePosition(10, 10), Horizontal)
        val rebalanced = rebalance(node)
        assertEquals(
            expected = "{ position: (10/10), orientation: Horizontal, left: noLeft, right: noRight }",
            actual = node.toString()
        )
        assertEquals(1, node.size())
        assertEquals(1, node.toList().size)
        assertEquals(1, rebalanced.size())
        assertEquals(1, rebalanced.toList().size)
    }

    @Test
    fun leftNode() {
        val node = Node(RelativePosition(10, 10), Horizontal).insert(RelativePosition(50, 0))
        assertEquals(
            expected = "{ position: (10/10), orientation: Horizontal, left: { position: (50/0), orientation: Vertical, left: noLeft, right: noRight }, right: noRight }",
            actual = node.toString()
        )
        assertEquals(2, node.size())
        assertEquals(2, node.toList().size)
    }

    @Test
    fun rightNode() {
        val node = Node(RelativePosition(10, 10), Horizontal).insert(RelativePosition(50, 100))
        assertEquals(
            expected = "{ position: (10/10), orientation: Horizontal, left: noLeft, right: { position: (50/100), orientation: Vertical, left: noLeft, right: noRight } }",
            actual = node.toString()
        )
        assertEquals(2, node.size())
        assertEquals(2, node.toList().size)
    }

    @Test
    fun bothNodes() {
        val order1 =
            Node(RelativePosition(10, 10), Horizontal).insert(RelativePosition(50, 100)).insert(RelativePosition(50, 0))
        val order2 =
            Node(RelativePosition(10, 10), Horizontal).insert(RelativePosition(50, 0)).insert(RelativePosition(50, 100))
        assertEquals(order1.toString(), order2.toString())
        assertEquals(
            expected = "{ position: (10/10), orientation: Horizontal, left: { position: (50/0), orientation: Vertical, left: noLeft, right: noRight }, right: { position: (50/100), orientation: Vertical, left: noLeft, right: noRight } }",
            actual = order1.toString()
        )
        assertEquals(order1.size(), order2.size())
        assertEquals(3, order1.size())
        assertEquals(3, order1.toList().size)
    }

    @Test
    fun deeperNodes() {
        val node = Node(RelativePosition(10, 10), Horizontal)
            .insert(RelativePosition(20, 20))
            .insert(RelativePosition(30, 30))
            .insert(RelativePosition(40, 40))
            .insert(RelativePosition(0, 0))
            .insert(RelativePosition(5, 5))
            .insert(RelativePosition(35, 5))
            .insert(RelativePosition(5, 35))
            .insert(RelativePosition(25, 25))
        val rebalanced = rebalance(rebalance(node))

        assertEquals(
            expected = "{ position: (10/10), orientation: Horizontal, left: { position: (0/0), orientation: Vertical, left: noLeft, right: { position: (5/5), orientation: Horizontal, left: noLeft, right: { position: (35/5), orientation: Vertical, left: noLeft, right: noRight } } }, right: { position: (20/20), orientation: Vertical, left: { position: (5/35), orientation: Horizontal, left: noLeft, right: noRight }, right: { position: (30/30), orientation: Horizontal, left: { position: (25/25), orientation: Vertical, left: noLeft, right: noRight }, right: { position: (40/40), orientation: Vertical, left: noLeft, right: noRight } } } }",
            actual = node.toString()
        )
        assertEquals(
            expected = "{ position: (20/20), orientation: Horizontal, left: { position: (10/10), orientation: Vertical, left: { position: (0/0), orientation: Horizontal, left: noLeft, right: { position: (5/5), orientation: Vertical, left: noLeft, right: noRight } }, right: { position: (35/5), orientation: Horizontal, left: noLeft, right: noRight } }, right: { position: (30/30), orientation: Vertical, left: { position: (25/25), orientation: Horizontal, left: noLeft, right: { position: (5/35), orientation: Vertical, left: noLeft, right: noRight } }, right: { position: (40/40), orientation: Horizontal, left: noLeft, right: noRight } } }",
            actual = rebalanced.toString()
        )
        assertEquals(9, node.size())
        assertEquals(9, node.toList().size)
        assertEquals(9, rebalanced.size())
        assertEquals(9, rebalanced.toList().size)
    }
}