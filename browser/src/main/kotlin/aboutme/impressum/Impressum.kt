package aboutme.impressum

import Classnames
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val ImpressumComponent = FC<Props> {
    div {
        id = "impressum"
        className = Classnames.text
        p {
            +"Dominik Leys"
        }
        p {
            +"dominik.Leys@gmail.com"
        }
        p {
            +"1100 Vienna"
        }
    }
}