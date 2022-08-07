package overview

import Classnames
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.useState

external interface HeaderProps : Props {
    var currentState: OverviewState
    var stateSetter: (OverviewState) -> Unit
}

val Header = FC<HeaderProps> { props ->
    val (collapsed, setCollapsed) = useState(true)

    div {
        css {
            width = 100.pct
        }
        id = "header"
        div {
            id = "hamburger-icon"
            +"☰"
            css {
                float = Float.left
            }
            onClick = {
                setCollapsed(!collapsed)
            }
        }
        h1 {
            className = Classnames.centeredContent
            +"Domo"
        }
    }

    div {
        id = "phone-menu"
        css {
            zIndex = integer(5)
            position = Position.absolute
            top = 40.px
            left = 5.px
            borderStyle = LineStyle.solid
            borderRadius = 10.px
            borderWidth = LineWidth.thin
            overflow = Overflow.hidden
        }
        if (collapsed) {
            className = Classnames.hidden
        }
        Menu {
            currentState = props.currentState
            stateSetter = {
                props.stateSetter(it)
                setCollapsed(!collapsed)
            }
        }
    }
}