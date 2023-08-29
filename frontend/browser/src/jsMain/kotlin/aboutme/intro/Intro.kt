package aboutme.intro

import aboutsite.AboutSiteMenu
import aboutsite.AboutSiteStates
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val IntroComponent = FC<Props> {
    div {
        p {
            +"Welcome, this site is under construction!"
        }

        br { }

        p {
            +"Dominik Leys"
        }

        p {
            +"Software Engineer from Vienna"
        }

        p {
            +"Experience: mostly Java, Kotlin and Scala"
        }

        br { }

        p {
            +"For an overview of this website visit the "
            a {
                href = "/${AboutSiteMenu.path}/${AboutSiteStates.Intro.path}"
                +AboutSiteMenu.text
            }
            +" page."
        }
    }
}