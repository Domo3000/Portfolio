package data

import kotlinx.css.*

typealias CSS = CssBuilder.() -> Unit

fun CssBuilder.styles(steps: Int = 4) {
    rule("*") {
        backgroundColor = Color.lightGray
        color = Color.black
        fontFamily = "Lucida Console, monospace"
        textAlign = TextAlign.left
    }

    button {
        textAlign = TextAlign.center
        fontWeight = FontWeight.bold
        fontFamily = "Monaco, monospace"
    }

    rule("#script-holder") {
        width = LinearDimension("100%")
        margin(0.px)
    }

    rule(".menu") {
        textAlign = TextAlign.left
        fontFamily = "Monaco, monospace"
        minWidth = LinearDimension("200px")
    }

    media("only screen and (min-width: 800px)") {
        rule(".phone-element") {
            display = Display.none
        }
    }

    media("only screen and (max-width: 800px)") {
        rule(".desktop-element") {
            display = Display.none
        }
        rule("#content-holder") {
            width = LinearDimension("100%")
        }
    }

    media("only screen and (max-width: 600px)") {
        rule(".phone-full-width") {
            width = LinearDimension("100% !important")
        }
    }

    // TODO get rid of this, use something better instead
    val begin = 800
    val end = 1200
    (1..steps).forEach { step ->
        val range = (end - begin) / steps
        val min = begin + range * (step - 1)
        val max = begin + range * step
        val pct = 60 + (20.0 / steps) * step

        media("only screen and (min-width: ${min}px) and (max-width: ${max}px)") {
            rule("#content-holder") {
                width = LinearDimension("${pct}%")
            }
        }
    }
}