class Deck(var elements: List<Int>) {
    val size
        get() = elements.size

    constructor(size: Int) : this((1..size).toList())

    fun sorted(): Boolean = this == Deck(size)

    fun randomize(): Deck {
        elements = elements.shuffled()

        return this
    }

    fun reset(size: Int): Deck {
        val new = Deck(size)
        elements = new.elements
        return this
    }

    fun sort(): Deck = reset(size)

    fun pile(n: Int): Deck {
        val builder = Array(n) { mutableListOf<Int>() }
        var count = 0
        var remainingElements = elements

        while (remainingElements.isNotEmpty()) {
            builder[count++ % n].add(remainingElements[0])
            remainingElements = remainingElements.drop(1)
        }

        elements = builder.toList().flatten()

        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as Deck

        if (size != other.size) return false
        elements.forEachIndexed { n, e ->
            if (e != other.elements[n]) {
                return false
            }
        }

        return true
    }

    override fun toString(): String = elements.joinToString(",", "{", "}")
}