package utils

abstract class WrappingArray<T>(val sizeX: Int, val sizeY: Int) {
    abstract val elements: ArrayList<ArrayList<T>>

    fun get(x: Int, y: Int, default: T? = null): T = if (default == null) {
        elements[y mod sizeY][x mod sizeX]
    } else {
        elements.getOrNull(y)?.getOrNull(x) ?: default
    }

    fun set(x: Int, y: Int, value: T) {
        elements[y][x] = value
    }

    fun setAll(method: (Int, Int) -> T) {
        (0 until sizeY).map { y ->
            (0 until sizeX).map { x ->
                elements[y][x] = method(x, y)
            }
        }
    }

}

fun <T> Array<T>.toArrayList(): ArrayList<T> = ArrayList(this.toList())