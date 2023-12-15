package projects

import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useEffectOnce
import react.useState
import web.cssom.Float
import web.cssom.FontWeight
import web.cssom.TextAlign
import web.cssom.pct
import web.dom.document
import web.events.Event
import web.events.EventType

enum class ProjectSubState {
    Play,
    About,
    Implementation
}

external interface ProjectOverviewProps : Props {
    var subState: ProjectSubState
    var parentRoute: String
}

abstract class ProjectOverview {
    abstract val aboutPage: FC<Props>
    abstract val implementationPage: FC<Props>
    abstract val playPage: FC<Props>
    open val header: String? = null

    val create: FC<ProjectOverviewProps>
        get() = FC { props ->
            val (state, setState) = useState(props.subState)

            ReactHTML.div {
                header?.let {
                    ReactHTML.h2 {
                        css {
                            textAlign = TextAlign.center
                        }
                        +it
                    }
                }
            }

            ReactHTML.div {
                ProjectSubState.entries.toList().forEach { contentState ->
                    ReactHTML.a {
                        href = "${props.parentRoute}/${contentState.toString().lowercase()}"
                        +contentState.toString()
                        css {
                            width = 33.pct
                            float = Float.left
                            textAlign = TextAlign.center
                            if (state == contentState) {
                                fontWeight = FontWeight.bold
                            }
                        }
                        onClick = {
                            it.preventDefault()
                            setState(contentState)
                        }
                    }
                }
            }

            ReactHTML.div {
                when (state) {
                    ProjectSubState.About -> aboutPage { }
                    ProjectSubState.Implementation -> implementationPage { }
                    ProjectSubState.Play -> playPage { }
                }
            }
        }
}

fun externalCanvas(name: String): FC<Props> = FC {
    ReactHTML.div {
        id = "external-holder"
    }

    useEffectOnce {
        document.dispatchEvent(Event(EventType(name)))

        cleanup {
            document.dispatchEvent(Event(EventType("${name}Cleanup")))
        }
    }
}