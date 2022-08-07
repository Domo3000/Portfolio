package menu

import csstype.FontWeight
import csstype.px
import emotion.react.css
import overview.OverviewState
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useState
import kotlin.reflect.KClass

external interface SubMenuElementProps : Props {
    var currentState: OverviewState
    var click: (SubmenuState) -> Unit
}

interface SubmenuState : OverviewState {
    val text: String

    val menu: FC<SubMenuElementProps>
        get() = FC { props ->
            div {
                +text

                onClick = {
                    props.click(this@SubmenuState)
                }

                css {
                    paddingLeft = 15.px

                    if (props.currentState == this@SubmenuState) {
                        fontWeight = FontWeight.bold
                    }
                }
            }
        }
}

external interface SubMenuProps : Props {
    var currentState: OverviewState
    var setState: (SubmenuState) -> Unit
}

abstract class SubMenu(private val initialSubState: SubmenuState) {
    abstract val text: String
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
                    element.menu {
                        currentState = props.currentState
                        click = {
                            props.setState(it)
                        }
                    }
                }
            }
        }
}