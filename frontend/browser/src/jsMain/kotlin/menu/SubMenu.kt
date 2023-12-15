package menu

import emotion.react.css
import kotlinx.browser.window
import overview.OverviewState
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import web.cssom.*
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
                    if (props.disabled != true) {
                        window.history.replaceState(Unit, "Domo", fullPath) // TODO use for Play/A/I Buttons in Project
                        props.setState(this@SubmenuState)
                    }
                }

                css {
                    display = Display.block
                    paddingLeft = 15.px

                    console.log(props.currentState)
                    console.log(props.currentState::class.toString())
                    console.log(props.currentState::class.simpleName)
                    console.log(this@SubmenuState)
                    console.log(this@SubmenuState::class.toString())
                    console.log(this@SubmenuState::class.simpleName)
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
                    element.element {
                        parentPath = path
                        currentState = props.currentState
                        setState = props.setState
                    }
                }
            }
        }
}