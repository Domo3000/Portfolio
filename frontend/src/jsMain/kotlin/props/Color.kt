package props

import web.cssom.Color

fun Triple<Int, Int, Int>.hslColor() = "hsl(${this.first},${this.second}%,${this.third}%)".unsafeCast<Color>()

fun Int.hslColor() = "hsl($this,100%,50%)".unsafeCast<Color>()