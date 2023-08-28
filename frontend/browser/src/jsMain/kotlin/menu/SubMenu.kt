package menu

import web.cssom.*
import emotion.react.css
import kotlinx.browser.window
import overview.OverviewState
import projects.ExternalProjectState
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import kotlin.reflect.KClass

external interface SubMenuElementProps : Props {
    var parentPath: String
    var disabled: Boolean?
    var currentState: OverviewState
    var setState: (SubmenuState) -> Unit
}

interface SubmenuState : OverviewState {
    val text: String
    val path: String

    val element: FC<SubMenuElementProps>
        get() = FC { props ->
            val fullPath = "/${props.parentPath}/${path}"
            ReactHTML.a {
                +text
                href = fullPath

                onClick = {
                    it.preventDefault()
                    if (props.disabled == false) {
                        window.history.replaceState(Unit, "Domo", fullPath)
                        props.setState(this@SubmenuState)
                    }
                }

                css {
                    display = Display.block
                    paddingLeft = 15.px

                    if (props.currentState == this@SubmenuState) {
                        fontWeight = FontWeight.bold
                    }

                    textDecoration = if (props.disabled == true) {
                        TextDecoration.lineThrough
                    } else {
                        None.none
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

abstract class SubMenu {
    abstract val text: String
    abstract val path: String
    abstract val matchingState: KClass<out SubmenuState>
    abstract val elements: List<SubmenuState>

    val create: FC<SubMenuProps>
        get() = FC { props ->
            val (collapsed, setCollapsed) = useState(true)
            ReactHTML.div {
                +text
                onClick = {
                    setCollapsed(!collapsed)
                }
            }
            if (!collapsed || matchingState.isInstance(props.currentState)) {
                elements.forEach { element ->
                    val maybeExternalState = element as? ExternalProjectState
                    val disabled =
                        (maybeExternalState != null && props.externalStates.contains(maybeExternalState.externalName))

                    element.element {
                        parentPath = path
                        this.disabled = disabled
                        currentState = props.currentState
                        setState = props.setState
                    }
                }
            }
        }
}