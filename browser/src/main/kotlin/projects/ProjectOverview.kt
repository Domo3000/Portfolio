package projects

import csstype.*
import emotion.react.css
import kotlinx.browser.document
import org.w3c.dom.events.Event
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffectOnce
import react.useState

sealed interface ProjectSubState

object ProjectSubStates {
    object Play : ProjectSubState
    object About : ProjectSubState
    object Implementation : ProjectSubState
}

abstract class ProjectOverview {
    abstract val aboutPage: FC<Props>
    abstract val implementationPage: FC<Props>
    abstract val playPage: FC<Props>

    private val states: Set<ProjectSubState> =
        setOf(ProjectSubStates.Play, ProjectSubStates.About, ProjectSubStates.Implementation)

    val create: FC<Props>
        get() = FC {
            val (state, setState) = useState<ProjectSubState>(ProjectSubStates.Play)

            ReactHTML.div {
                css {
                    maxWidth = 1000.px
                }

                states.forEach { contentState ->
                    ReactHTML.div {
                        +contentState::class.simpleName!!
                        css {
                            width = 33.pct
                            float = Float.left
                            textAlign = TextAlign.center
                            if (state == contentState) {
                                fontWeight = FontWeight.bold
                            }
                        }
                        onClick = {
                            setState(contentState)
                        }
                    }
                }
            }

            ReactHTML.div {
                css {
                    maxWidth = 1000.px
                }

                when (state) {
                    ProjectSubStates.About -> aboutPage { }
                    ProjectSubStates.Implementation -> implementationPage { }
                    ProjectSubStates.Play -> playPage { }
                }
            }
        }
}

fun externalCanvas(name: String): FC<Props> = FC {
    ReactHTML.div {
        id = "external-holder"
    }

    useEffectOnce {
        document.dispatchEvent(Event(name))

        cleanup {
            document.dispatchEvent(Event("${name}Cleanup"))
        }
    }
}