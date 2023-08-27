package props

import web.cssom.Color

fun Int.hslColor() = "hsl($this,100%,50%)".unsafeCast<Color>()