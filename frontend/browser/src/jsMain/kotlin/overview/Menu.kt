package overview

import aboutme.AboutMeMenu
import aboutsite.AboutSiteMenu
import emotion.react.css
import projects.ProjectsMenu
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Float
import web.cssom.px

external interface MenuProps : Props {
    var currentState: OverviewState
    var stateSetter: (OverviewState) -> Unit
    var externalStates: List<String>
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

        AboutSiteMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
        }

        ProjectsMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
        }
    }
}
