package automaton

import kotlin.math.pow

object Rule {
    fun toNumber(left: Boolean, middle: Boolean, right: Boolean) = (left times 4) + (middle times 2) + (right times 1)
}

infix fun Boolean.times(n: Int): Int = if (this) n else 0

fun Int.contains(n: Int) = (this and 2.0.pow(n).toInt()) != 0

fun Int.switch(n: Int) = this xor 2.0.pow(n).toInt()