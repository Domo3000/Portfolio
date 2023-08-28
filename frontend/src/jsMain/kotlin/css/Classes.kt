package css

import csstype.PropertiesBuilder
import web.cssom.*

private typealias CSS = PropertiesBuilder.() -> Unit

object Classes {
    val canvas: CSS = {
        width = 100.pct
        maxWidth = 1000.px
        borderStyle = LineStyle.solid
        borderWidth = LineWidth.thin
        backgroundColor = NamedColor.white
    }

    val hidden: CSS = {
        display = None.none
    }
}