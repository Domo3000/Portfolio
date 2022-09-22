package overview

import aboutme.AboutMeMenu
import css.Classes
import aboutme.AboutMeStates
import csstype.pct
import csstype.px
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
        id = "footer"
        css {
            width = 100.pct
            maxHeight = 50.px
            padding = 15.px
            marginTop = 200.px
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