package menu

import emotion.react.css
import kotlinx.browser.window
import overview.OverviewState
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.None
import web.cssom.px

external interface RecursiveSubMenuProps : Props {
    var currentState: OverviewState
    var setState: (OverviewState) -> Unit
}

abstract class RecursiveSubMenu {
    abstract val text: String
    abstract val path: String
    abstract val elements: List<RecursiveSubMenu>
    open val parentPath: String = ""
    open val state: OverviewState? = null

    val fullPath
        get() = "${parentPath}/${path}"

    private fun matchesState(currentState: OverviewState): Boolean =
        (currentState == state) || elements.any { it.matchesState(currentState) }

    val create: FC<RecursiveSubMenuProps>
        get() = FC { props ->
            val (collapsed, setCollapsed) = useState(true)
            ReactHTML.a {
                +text
                href = fullPath
                css {
                    textDecoration = None.none
                    if (matchesState(props.currentState)) {
                        fontWeight = FontWeight.bold
                    }
                }

                onClick = {
                    it.preventDefault()
                    if (collapsed) {
                        state?.let { overviewState ->
                            window.history.replaceState(Unit, "Domo", fullPath)
                            props.setState(overviewState)
                        }
                    }
                    setCollapsed(!collapsed)
                }
            }
            ReactHTML.div {
                css {
                    display = Display.block
                    paddingLeft = 15.px
                }
                if (!collapsed || matchesState(props.currentState)) {
                    elements.forEach { element ->
                        element.create {
                            currentState = props.currentState
                            setState = { props.setState(it) }
                        }
                    }
                }
            }
        }
}