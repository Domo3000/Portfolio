package aboutme.impressum

import web.cssom.None
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val ImpressumComponent = FC<Props> {
    div {
        p {
            +"Dominik Leys"
        }
        a {
            css {
                textDecoration = None.none
            }
            href = "mailto:dominik.leys@gmail.com"
            +"dominik.leys@gmail.com"
        }
        p {
            +"1100 Vienna"
        }
    }
}