package canvas

import csstype.NamedColor
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.floor

fun HTMLCanvasElement.getElementWidth(size: Int) = width.toDouble() / size

fun HTMLCanvasElement.getX(x: Double, size: Int) =
    (x / getElementWidth(size)).toInt()

fun HTMLCanvasElement.getRelativeX(x: Int, size: Int) =
    x * getElementWidth(size)

fun HTMLCanvasElement.getElementHeight(size: Int) = height.toDouble() / size

fun HTMLCanvasElement.getY(y: Double, size: Int) =
    (y / getElementHeight(size)).toInt()

fun HTMLCanvasElement.getRelativeY(y: Int, size: Int) =
    y * getElementHeight(size)

fun HTMLCanvasElement.resetBitmapSize() {
    width = getBoundingClientRect().width.toInt()
}

fun HTMLCanvasElement.resetDimensions(ratio: Double = 3.0 / 4.0) {
    resetBitmapSize()
    height = (width * ratio).toInt()
}

fun HTMLCanvasElement.setDimensions(width: Int = 800, height: Int = 600) {
    this.width = width
    this.height = height
}

fun CanvasRenderingContext2D.drawBackground(borderWidth: Double = 1.0) {
    fillStyle = NamedColor.black
    fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    fillStyle = NamedColor.white
    fillRect(
        borderWidth,
        borderWidth,
        canvas.width - borderWidth * 2,
        canvas.height - borderWidth * 2
    )
}

fun CanvasRenderingContext2D.clear() {
    clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
}

fun CanvasRenderingContext2D.drawRectangle(x: Int, y: Int, sizeX: Int, sizeY: Int, width: Int, height: Int, color: String) {
    val elementWidth = width.toDouble() / sizeX
    val elementHeight = height.toDouble() / sizeY

    fillStyle = color
    fillRect(
        floor(x * elementWidth),
        floor(y * elementHeight),
        floor(elementWidth),
        floor(elementHeight)
    )
}