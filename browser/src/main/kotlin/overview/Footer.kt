package overview

import aboutme.AboutMeMenu
import aboutme.AboutMeStates
import css.Classes
import csstype.Clear
import emotion.react.css
import kotlinx.browser.window
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h6

external interface FooterProps : Props {
    var stateSetter: (OverviewState) -> Unit
}

val Footer = FC<FooterProps> { props ->
    div {
        css {
            clear = Clear.left
        }
        h6 {
            css(Classes.centered)
            onClick = {
                window.history.replaceState(Unit, "Domo", "/${AboutMeMenu.path}/${AboutMeStates.Impressum.path}")
                props.stateSetter(AboutMeStates.Impressum)
            }
            +"Impressum"
        }
    }
}