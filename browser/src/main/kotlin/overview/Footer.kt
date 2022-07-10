package overview

import Classnames
import aboutme.AboutMeStates
import csstype.pct
import csstype.px
import emotion.react.css
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
        }
        h6 {
            className = Classnames.centeredContent
            onClick = {
                props.stateSetter(AboutMeStates.Impressum)
            }
            +"Impressum"
        }
    }
}