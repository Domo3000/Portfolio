package overview

import aboutme.AboutMeMenu
import aboutpage.AboutPageMenu
import csstype.Float
import csstype.px
import emotion.react.css
import projects.ProjectsMenu
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface MenuProps : Props {
    var currentState: OverviewState
    var stateSetter: (OverviewState) -> Unit
}

val Menu = FC<MenuProps> { props ->
    div {
        css {
            width = 200.px
            float = Float.left
            paddingLeft = 10.px
        }

        AboutMeMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
        }

        AboutPageMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
        }

        ProjectsMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
        }
    }
}
