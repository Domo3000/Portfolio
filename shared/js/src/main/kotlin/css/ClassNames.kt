package css

import csstype.ClassName

object ClassNames {
    val phoneElement = ClassName("phone-element")
    val desktopElement = ClassName("desktop-element")
    val phoneFullWidth = ClassName("phone-full-width")
}

infix fun ClassName.and(s: String) = ClassName("${this.unsafeCast<String>()} $s")