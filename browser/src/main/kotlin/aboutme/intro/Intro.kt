package aboutme.intro

import Classnames
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val IntroComponent = FC<Props> {
    div {
        className = Classnames.text

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