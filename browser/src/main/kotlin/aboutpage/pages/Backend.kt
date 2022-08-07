package aboutpage.pages

import Classnames
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.ul

val BackendComponent = FC<Props> {
    div {
        className = Classnames.text

        p {
            a {
                href = "https://kotlinlang.org/docs/server-overview.html"
                +"Kotlin/JVM"
            }
        }

        p {
            +"A small "
            a {
                href = "https://github.com/Domo3000/Portfolio/tree/main/server"
                +"server"
            }
            +" that uses "
            a {
                href = "https://ktor.io/"
                +"Ktor"
            }
            +" to handle requests."
        }

        p {
            +"Planned features:"
        }
        ul {
            li {
                +"unit tests"
            }
            li {
                +"handling websockets for Connect4 game"
            }
            li {
                +"randomized and simple AI for Connect4 game"
            }
            li {
                +"neural network for Connect4 game"
            }
        }
    }
}