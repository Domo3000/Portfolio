package projects.shuffle

import react.FC
import react.Props
import react.dom.html.ReactHTML
import utils.Github
import web.window.WindowTarget

val Implementation = FC<Props> {
    ReactHTML.div {
        ReactHTML.p {
            ReactHTML.a {
                href = Github.link("tree/main/frontend/canvas/shuffle")
                target = WindowTarget._blank
                +"Source Code"
            }
        }
        ReactHTML.p {
            +"TODO explanation of Code"
        }
        ReactHTML.p {
            +"Creating abstract ExternalCanvas class to move project code easier into own modules"
        }
        ReactHTML.p {
            +"Figuring out how to run it relatively efficient in the About page"
        }
        ReactHTML.p {
            +"The implementation included:"
        }
    }
}