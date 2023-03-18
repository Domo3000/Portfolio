package projects.connect4

import react.FC
import react.Props
import react.dom.html.ReactHTML

val Implementation = FC<Props> {
    ReactHTML.h4 {
        +"Connect 4 Implementation"
    }
    ReactHTML.p {
        ReactHTML.a {
            href = "https://github.com/Domo3000/Portfolio/tree/main/connect4/src/main/kotlin"
            +"Source Code"
        }
    }
    ReactHTML.p {
        +"TODO link to Frontend and Backend codes as well, and explain more stuff."
    }
    ReactHTML.details {
        ReactHTML.summary {
            +"Algorithms"
        }
        ReactHTML.p {
            +"TODO explain LengthAI and MonteCarloAI"
        }
    }
    ReactHTML.details {
        ReactHTML.summary {
            +"Neural Networks"
        }
        ReactHTML.p {
            +"TODO link to kotlindl"
        }
    }
    ReactHTML.details {
        ReactHTML.summary {
            +"Websockets"
        }
        ReactHTML.p {
            +"TODO links to Ktor Websocket pages"
        }
    }
}