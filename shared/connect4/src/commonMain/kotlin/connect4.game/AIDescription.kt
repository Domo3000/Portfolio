package connect4.game

enum class InputType {
    SingularMinus,
    SingularPlus,
    DualNeutral,
    DualMinus,
    DualPlus;
    fun toShortString() = when(this) {
        SingularMinus -> "T"
        DualNeutral -> "F"
        SingularPlus -> "A"
        DualMinus -> "B"
        DualPlus -> "C"
    }

    companion object {
        fun fromShortString(short: String) =
            InputType.entries.find { it.toShortString() == short }!!
    }
}

enum class AIChoice {
    Simple,
    Medium,
    Hard,
    MonteCarlo,
    Length,
    Neural
}

enum class Activation {
    Elu,
    LiSHT,
    Mish,
    Relu,
    Snake;

    fun toShortString() = "${toString().first()}"

    companion object {
        fun fromShortString(short: String) =
            entries.find { it.toShortString() == short }!!
    }
}

enum class OutputActivation {
    Linear,
    Relu,
    Sigmoid,
    Softmax,
    Tanh;

    fun toShortString() = toString().take(2)

    companion object {
        fun fromShortString(short: String) =
            entries.find { it.toShortString() == short }!!
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
    }
}

enum class Padding {
    Same,
    Valid;

    fun toShortString() = "${toString().first()}"

    companion object {
        fun fromShortString(short: String): Padding =
            entries.find { it.toShortString() == short }!!
    }
}
