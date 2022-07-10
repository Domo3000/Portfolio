package projects.automaton

import Classnames
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useState

sealed interface AutomatonState : Props

object AutomatonStates {
    object Play : AutomatonState
    object About : AutomatonState
    object Implementation : AutomatonState
}

val AutomatonOverview = FC<Props> { // TODO extract to interface
    val (state, setState) = useState<AutomatonState>(AutomatonStates.Play)

    div {
        className = Classnames.contentHeader
        div {
            +"Play"
            css {
                width = 30.pct // TODO CSS STYLE
                float = Float.left
                textAlign = TextAlign.center
            }
            onClick = {
                setState(AutomatonStates.Play)
            }
        }
        div {
            +"About" // TODO explain RGB instead RBY
            css {
                width = 30.pct
                float = Float.left
                textAlign = TextAlign.center
            }
            onClick = {
                setState(AutomatonStates.About)
            }
        }
        div {
            +"Code"
            css {
                width = 30.pct
                float = Float.left
                textAlign = TextAlign.center
            }
            onClick = {
                setState(AutomatonStates.Implementation)
            }
        }
    }


    div {
        className = Classnames.content

        css {
            width = 100.pct
        }

        when (state) {
            AutomatonStates.About -> div {
                className = Classnames.text
                +"TODO explanation of Automaton"
            }
            AutomatonStates.Implementation -> div {
                className = Classnames.text
                +"TODO explanation of implementation"
            }
            AutomatonStates.Play -> Canvas { }
        }
    }
}