package overview

import aboutme.AboutMeStates
import css.ClassNames
import css.and
import emotion.react.css
import kotlinx.browser.document
import projects.ProjectStates
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffectOnce
import react.useState
import web.cssom.Float
import web.cssom.pct
import web.cssom.px

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
            +"404 - Not Found"
        }
    }
}

fun overview(component: OverviewState = AboutMeStates.Intro) = FC<Props> {
    val (state, setState) = useState(component)

    val (loadingExternalScripts, setLoadingExternalScripts) = useState(
        ProjectStates.states.map { it.externalName }
    )
    /**
     *  To prevent race conditions from causing issues ExternalCanvas sends <name>Initialized twice.
     *  If we remove the contains() or check on loadingExternalScripts the pages get reloaded if they get enabled again.
     *  Play around with the Debug Project for an explanation.
     */
    val mutableExternalScripts = loadingExternalScripts.toMutableList()

    fun enable(key: String) {
        if (mutableExternalScripts.contains(key)) {
            mutableExternalScripts.remove(key)
            setLoadingExternalScripts { l -> l - key }
        }
    }

    Header {
        currentState = state
        stateSetter = { setState(it) }
        externalStates = loadingExternalScripts
    }

    div {
        div {
            css(ClassNames.desktopElement and "menu") {
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
                maxWidth = 1000.px
                minHeight = 600.px
                paddingBottom = 100.px
                float = Float.left
            }

            //val maybeExternalState = state as? ExternalProjectState
            /* TODO fix
            val maybeExternalState = state as? ProjectState

            if (maybeExternalState == null || !loadingExternalScripts.contains(maybeExternalState.externalName)) {
              */
                state.component {}
            /*
            } else {
                LoadingScreen().component { title = maybeExternalState.text }
            }

             */
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
