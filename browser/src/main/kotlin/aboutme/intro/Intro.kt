package aboutme.intro

import aboutpage.AboutPageMenu
import aboutpage.AboutPageStates
import css.Classes
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.br
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

        br { }

        p {
            +"For an overview of this website visit the "
            a {
                href = "/${AboutPageMenu.path}/${AboutPageStates.Intro.path}"
                +"About this page"
            }
            +" section."
        }
    }
}