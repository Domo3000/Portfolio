package overview

import Classnames
import csstype.Float
import csstype.pct
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