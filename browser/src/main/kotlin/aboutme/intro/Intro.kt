package aboutme.intro

import css.Classes
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val IntroComponent = FC<Props> {
    div {
        css(Classes.text)

        p {
            +"Welcome, this page is under construction!"
        }

        p {
            +"Dominik Leys"
        }

        p {
            +"Software Engineer, Vienna"
        }

        p {
            +"Experience: Java, Kotlin, Scala"
        }
    }
}