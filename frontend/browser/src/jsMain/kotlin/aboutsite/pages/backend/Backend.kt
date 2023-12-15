package aboutsite.pages.backend

import projects.ProjectStates
import projects.ProjectsMenu
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.summary
import utils.Github
import web.window.WindowTarget

val BackendComponent = FC<Props> {
    div {
        h3 {
            a {
                href = "https://kotlinlang.org/docs/server-overview.html"
                target = WindowTarget._blank
                +"Kotlin/JVM"
            }
        }

        p {
            +"A small "
            a {
                href = Github.link("tree/main/server")
                target = WindowTarget._blank
                +"server"
            }
            +" that uses "
            a {
                href = "https://ktor.io/"
                target = WindowTarget._blank
                +"Ktor"
            }
            +" to handle requests."
        }

        p {
            +"Some features:"
        }
        details {
            summary {
                +"Deep Learning"
            }
            p {
                +"The "
                a {
                    href = "/${ProjectsMenu.path}/${ProjectStates.Connect4.path}"
                    +"Connect4"
                }
                +"  project trains "
                a {
                    href = Github.link("tree/main/server/src/main/kotlin/connect4/ai/neural")
                    target = WindowTarget._blank
                    +"Neural Networks"
                }
                +" how to play that game."
            }
        }
        details {
            summary {
                +"Websockets"
            }
            p {
                +"The Connect4 project uses "
                a {
                    href = Github.link("blob/main/server/src/main/kotlin/connect4/Connect4SocketHandler.kt")
                    target = WindowTarget._blank
                    +"Websockets"
                }
                +" to communicate with the "
                a {
                    href = Github.link("blob/main/frontend/canvas/connect4/src/jsMain/kotlin/util/SocketClient.kt")
                    target = WindowTarget._blank
                    +"Frontend"
                }
                +"."
            }
        }
    }
}