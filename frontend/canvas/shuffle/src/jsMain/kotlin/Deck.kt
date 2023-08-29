private fun new(size: Int) = (1..size).toList()

private fun toModulo(inShuffle: Boolean): (Int, Int) -> Int = if (inShuffle) {
    { i: Int, n: Int -> (i % n) }
} else {
    { i: Int, n: Int -> (n - (i % n)) }
}

class Deck(var elements: List<Int>, var modulo: (Int, Int) -> Int) {
    val size
        get() = elements.size

    constructor(size: Int, inShuffle: Boolean) : this(new(size), toModulo(inShuffle))

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
        elements = new(size)
    }

    fun sort() = reset(size)

    fun setModulo(inShuffle: Boolean) {
        modulo = toModulo(inShuffle)
    }

    fun pile(piles: Int) {
        val builder = Array<Int?>(size + piles) { null }

        elements.forEachIndexed { i, e ->
            builder[modulo(i, piles) * ((size + piles) / piles) + i / piles] = e
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

    fun pilesString(piles: Int): String {
        val builder: List<MutableList<Int>> = Array<MutableList<Int>>(piles) {
            mutableListOf()
        }.toList()

        elements.forEachIndexed { i, e ->
            builder[i % piles].add(e)
        }

        return builder.mapIndexed { index, list -> index to list }.joinToString(", ") { (index, list) ->
            "Pile${index + 1}:${list.joinToString(", ", "[", "]")}"
        }
    }

    override fun toString(): String = "Deck:${elements.joinToString(", ", "[", "]")}"
}