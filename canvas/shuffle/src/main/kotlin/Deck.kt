class Deck(var elements: List<Int>) {
    val size
        get() = elements.size

    constructor(size: Int) : this((1..size).toList())

    fun sorted(): Boolean {
        elements.forEachIndexed { i, e ->
            if (e != (i + 1)) {
                return false
            }
        }
        return true
    }

    fun randomize() {
        elements = elements.shuffled()
    }

    fun reset(size: Int) {
        val new = Deck(size)
        elements = new.elements
    }

    fun sort() = reset(size)

    fun pile(n: Int) {
        val builder = Array<Int?>(size + n) { null }

        elements.forEachIndexed { i, e ->
            builder[(i % n) * ((size + n) / n) + i / n] = e
        }

        elements = builder.filterNotNull().toList()
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