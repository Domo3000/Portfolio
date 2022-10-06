package react

import csstype.Color

fun Int.hslColor() = "hsl($this,100%,50%)".unsafeCast<Color>()