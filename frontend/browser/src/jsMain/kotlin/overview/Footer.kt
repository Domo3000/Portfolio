package overview

import aboutme.AboutMeMenu
import aboutme.AboutMeStates
import web.cssom.*
import emotion.react.css
import kotlinx.browser.window
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div

external interface FooterProps : Props {
    var stateSetter: (OverviewState) -> Unit
}

val Footer = FC<FooterProps> { props ->
    val impressumPath = "/${AboutMeMenu.path}/${AboutMeStates.Impressum.path}"
    div {
        css {
            clear = Clear.left
        }
        ReactHTML.a {
            css {
                display = Display.block
                margin = Auto.auto
                maxWidth = 200.px
                textAlign = TextAlign.center
                textDecoration = None.none
                fontWeight = FontWeight.bolder
                fontSize = (2.0/3.0).em
            }
            href = impressumPath
            onClick = {
                it.preventDefault()
                window.history.replaceState(Unit, "Domo", impressumPath)
                props.stateSetter(AboutMeStates.Impressum)
            }
            +"Impressum"
        }
    }
}