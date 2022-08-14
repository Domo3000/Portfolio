package overview

import aboutme.AboutMeStates
import css.Classes
import csstype.Float
import csstype.pct
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useState

interface OverviewState {
    val component: FC<Props>
}

val Overview = FC<Props> {
    val (state, setState) = useState<OverviewState>(AboutMeStates.Intro)

    div {
        Header {
            currentState = state
            stateSetter = { setState(it) }
        }
    }

    div {
        div {
            id = "desktop-menu"
            css {
                width = 20.pct
                float = Float.left
            }
            Menu {
                currentState = state
                stateSetter = { setState(it) }
            }
        }
        div {
            id = "content-holder"
            css {
                width = 80.pct
                minHeight = 600.px
                float = Float.left
            }
            state.component {}
        }
    }
    Footer { stateSetter = { setState(it) } }
}
