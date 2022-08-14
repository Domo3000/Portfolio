package aboutme.impressum

import css.Classes
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val ImpressumComponent = FC<Props> {
    div {
        id = "impressum"
        css(Classes.text)

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