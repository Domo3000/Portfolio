package overview

import aboutme.AboutMeStates
import css.Classes
import csstype.Float
import csstype.pct
import csstype.px
import emotion.react.css
import kotlinx.browser.document
import projects.ExternalProjectState
import projects.ProjectStates
import react.*
import react.dom.html.ReactHTML.div

interface OverviewState {
    val component: FC<Props>
}

external interface LoadingScreenProps : Props {
    var title: String
}

class LoadingScreen {
    val component: FC<LoadingScreenProps>
        get() = FC { props ->
            div {
                +"loading ${props.title}"
            }
        }
}

object NotFoundState : OverviewState {
    override val component = FC<Props> {
        div {
            css(Classes.text)
            +"404 - Not Found"
        }
    }
}

fun overview(component: OverviewState = AboutMeStates.Intro) = FC<Props> {
    val (state, setState) = useState(component)
    val (loadingExternalScripts, setExternalScripts) = useState(
        ProjectStates.states.map { it.externalName }
    )

    fun enable(key: String) {
        setExternalScripts { l -> l - key }
    }

    div {
        Header {
            currentState = state
            stateSetter = { setState(it) }
            externalStates = loadingExternalScripts
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
                externalStates = loadingExternalScripts
            }
        }
        div {
            id = "content-holder"
            css {
                width = 80.pct
                minHeight = 600.px
                float = Float.left
            }

            val maybeExternalState = state as? ExternalProjectState

            if (maybeExternalState == null || !loadingExternalScripts.contains(maybeExternalState.externalName)) {
                state.component {}
            } else {
                LoadingScreen().component { title = maybeExternalState.text }
            }
        }
    }
    Footer { stateSetter = { setState(it) } }

    useEffectOnce {
        loadingExternalScripts.forEach { key ->
            document.addEventListener("${key}Initialized", {
                enable(key)
            })
        }

        if (component != state) {
            setState(component)
        }
    }
}
