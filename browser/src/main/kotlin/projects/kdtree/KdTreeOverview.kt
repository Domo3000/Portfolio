package projects.kdtree

import Classnames
import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.useState

sealed interface KdTreeState

object KdTreeStates {
    object Play : KdTreeState
    object About : KdTreeState
    object Code : KdTreeState
    
    val states = listOf(Play, About, Code)
}

val KdTreeOverview = FC<Props> {  // TODO abstract Overview class, also Bold currentState
    val (state, setState) = useState<KdTreeState>(KdTreeStates.Play)

    div {
        className = Classnames.contentHeader
        
        KdTreeStates.states.forEach { contentState ->
            div {
                +contentState::class.simpleName!!
                css {
                    width = 30.pct
                    float = Float.left
                    textAlign = TextAlign.center
                }
                onClick = {
                    setState(contentState)
                }
            }
        }
    }
    
    div {
        className = Classnames.content

        css {
            width = 100.pct
        }

        when (state) {
            KdTreeStates.About -> p {
                className = Classnames.text
                +"TODO explanation of KdTrees"
            }
            KdTreeStates.Code -> p {
                className = Classnames.text
                +"TODO explanation of implementation"
            }
            KdTreeStates.Play -> Canvas { tree = null }
        }
    }
}