package menu

import csstype.FontWeight
import csstype.TextDecoration
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import overview.OverviewState
import projects.ExternalProjectState
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useState
import kotlin.reflect.KClass

external interface SubMenuElementProps : Props {
    var currentState: OverviewState
    var disabled: Boolean?
    var click: (SubmenuState) -> Unit
}

interface SubmenuState : OverviewState {
    val text: String
    val path: String

    val element: FC<SubMenuElementProps>
        get() = FC { props ->
            div { // TODO a with href # and no design
                +text

                onClick = {
                    props.click(this@SubmenuState)
                }

                css {
                    paddingLeft = 15.px

                    if (props.currentState == this@SubmenuState) {
                        fontWeight = FontWeight.bold
                    }

                    if (props.disabled == true) {
                        textDecoration = TextDecoration.lineThrough
                    }
                }
            }
        }
}

external interface SubMenuProps : Props {
    var currentState: OverviewState
    var setState: (SubmenuState) -> Unit
    var externalStates: List<String>
}

abstract class SubMenu(private val initialSubState: SubmenuState) {
    abstract val text: String
    abstract val path: String
    abstract val matchingState: KClass<out SubmenuState>
    abstract val elements: List<SubmenuState>

    val create: FC<SubMenuProps>
        get() = FC { props ->
            val (collapsed, setCollapsed) = useState(true)
            div {
                +text
                onClick = {
                    setCollapsed(!collapsed)
                }
            }
            if (!collapsed || matchingState.isInstance(props.currentState)) {
                elements.forEach { element ->
                    val maybeExternalState = element as? ExternalProjectState
                    if (maybeExternalState != null && props.externalStates.contains(maybeExternalState.externalName)) {
                        element.element {
                            currentState = props.currentState
                            disabled = true
                            click = { }
                        }
                    } else {
                        element.element {
                            currentState = props.currentState
                            click =
                                {
                                    window.history.replaceState(Unit, "Domo", "/$path/${element.path}")
                                    props.setState(it)
                                }
                        }
                    }
                }
            }
        }
}