package css

import csstype.*

private typealias CSS = PropertiesBuilder.() -> Unit

object Classes {
    val weird: CSS = {
        fontSize = FontSize.xLarge
    }

    val centered: CSS = {
        margin = Auto.auto
        maxWidth = 200.px
        textAlign = TextAlign.center
    }

    val text: CSS = {
        textAlign = TextAlign.left
        fontFamily = string("\"Lucida Console\", monospace")
    }

    val canvas: CSS = {
        width = 100.pct
        maxWidth = 1000.px
    }

    val hidden: CSS = {
        display = None.none
    }
}