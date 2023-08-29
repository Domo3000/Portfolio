package projects.connect4

import react.FC
import react.Props
import web.window.WindowTarget
import react.dom.html.ReactHTML
import utils.Github

val Implementation = FC<Props> {
    ReactHTML.h4 { // TODO style guide -> what looks better, strong or h4?
        +"Connect 4 Implementation"
    }
    ReactHTML.p {
        ReactHTML.a {
            href = Github.link("tree/main/shared/connect4")
            target = WindowTarget._blank
            +"Common Source Code"
        }
    }
    ReactHTML.p {
        ReactHTML.a {
            href = Github.link("tree/main/frontend/canvas/connect4")
            target = WindowTarget._blank
            +"Frontend Source Code"
        }
    }
    ReactHTML.p {
        ReactHTML.a {
            href = Github.link("tree/main/server/src/main/kotlin/connect4")
            target = WindowTarget._blank
            +"Backend Source Code"
        }
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
        ReactHTML.p {
            +"TODO explain link to various training techniques"
        }
        ReactHTML.p {
            +"TODO example network layout"
        }
        ReactHTML.p {
            +"TODO explain evolution"
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