package projects.labyrinth

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML
import utils.Github
import web.window.WindowTarget

object LabyrinthOverview : ProjectOverview() {
    override val header = "Labyrinth"
    override val aboutPage: FC<Props>
        get() = FC {
            ReactHTML.div {
                +"This is a small prototype of a Mobile Game I'm currently developing."
            }
        }
    override val implementationPage: FC<Props>
        get() = FC {
            ReactHTML.div {
                ReactHTML.p {
                    ReactHTML.a {
                        href = Github.link("tree/main/frontend/canvas/labyrinth")
                        target = WindowTarget._blank
                        +"Source Code"
                    }
                }
                +"TODO link to Open Source Code of the Mobile Game once it's released"
            }
        }
    override val playPage: FC<Props>
        get() = externalCanvas("Labyrinth")
}