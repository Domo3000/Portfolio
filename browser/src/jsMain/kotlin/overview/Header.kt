package overview

import css.Classes
import css.ClassNames
import css.and
import web.cssom.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.useState

external interface HeaderProps : Props {
    var currentState: OverviewState
    var stateSetter: (OverviewState) -> Unit
    var externalStates: List<String>
}

val Header = FC<HeaderProps> { props ->
    val (collapsed, setCollapsed) = useState(true)

    div {
        css {
            width = 100.pct
        }
        id = "header"
        div {
            +"☰"
            css(ClassNames.phoneElement) {
                textAlign = TextAlign.center
                float = Float.left
                minWidth = 30.px
                minHeight = 30.px
            }
            onClick = {
                setCollapsed(!collapsed)
            }
        }
        h1 {
            css {
                textAlign = TextAlign.center
                fontFamily = string("\"Garamond \", serif")  // TODO different font
            }
            +"Domo"
        }
    }

    div {
        if (collapsed) {
            css(Classes.hidden)
        } else {
            css(ClassNames.phoneElement and "menu") {
                zIndex = integer(5)
                position = Position.absolute
                top = 40.px
                left = 5.px
                borderStyle = LineStyle.solid
                borderRadius = 10.px
                borderWidth = LineWidth.thin
                overflow = Overflow.hidden
            }
        }
        Menu {
            currentState = props.currentState
            stateSetter = {
                props.stateSetter(it)
                setCollapsed(!collapsed)
            }
            externalStates = props.externalStates
        }
    }
}