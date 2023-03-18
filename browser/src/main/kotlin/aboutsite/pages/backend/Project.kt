package aboutsite.pages.backend

import aboutsite.AboutSiteMenu
import aboutsite.AboutSiteStates
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p

val ProjectComponent = FC<Props> {
    h3 {
        a {
            href = "https://kotlinlang.org/docs/multiplatform.html"
            +"Kotlin Multiplatform"
        }
    }
    p {
        +"This whole website was fully written in Kotlin."
    }
    p {
        +"The React "
        a {
            href = "/${AboutSiteMenu.path}/${AboutSiteStates.Frontend.path}"
            +"Frontend"
        }
        +"gets compiled to Javascript and the Ktor "
        a {
            href = "/${AboutSiteMenu.path}/${AboutSiteStates.Backend.path}"
            +"Backend"
        }
        +" Server runs as a .jar file."
    }
    p {
        +"I'm using the "
        a {
            href = "https://github.com/johnrengelman/shadow"
            +"Shadow Plugin"
        }
        +" to create a Fat JAR."
    }
}

/*
    TODO print project structure as a nicely formatted vertical tree
 */