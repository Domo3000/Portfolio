package aboutsite.pages

import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p

val BackendComponent = FC<Props> {
    div {
        h3 {
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
        list {
            texts = listOf(
                "unit tests",
                "handling websockets for Connect4 game",
                "randomized and simple AI for Connect4 game",
                "neural network for Connect4 game"
            )
        }
    }
}