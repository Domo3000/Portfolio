package connect4.messages

enum class AIChoice {
    Simple,
    Medium,
    Hard,
    MonteCarlo,
    Length,
    Neural
}

enum class Activation {
    LiSHT,
    Elu,
    Swish,
    Mish,
    Relu;

    fun toShortString() = "${toString().first()}"

    companion object {
        fun fromShortString(short: String) =
            entries.find { it.toShortString() == short }!!

        val neutral = Swish
    }
}

enum class LayerSize {
    None,
    Small,
    Medium,
    Large,
    Giant;

    fun toShortString() = "${toString().first()}"

    companion object {
        fun fromShortString(short: String) =
            entries.find { it.toShortString() == short }!!

        val neutral = Medium
    }
}
