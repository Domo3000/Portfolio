package overview

import aboutme.AboutMeMenu
import aboutsite.AboutSiteMenu
import web.cssom.px
import emotion.react.css
import projects.ProjectsMenu
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import web.cssom.Float

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
            externalStates = props.externalStates
        }

        AboutSiteMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
            externalStates = props.externalStates
        }

        ProjectsMenu.create {
            currentState = props.currentState
            setState = { props.stateSetter(it) }
            externalStates = props.externalStates
        }
    }
}
