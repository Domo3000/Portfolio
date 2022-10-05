package projects.labyrinth

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML

object LabyrinthOverview : ProjectOverview() {
    override val header = "Labyrinth"
    override val aboutPage: FC<Props>
        get() = FC {
            ReactHTML.div {
                +"TODO"
            }
        }
    override val implementationPage: FC<Props>
        get() = FC {
            ReactHTML.div {
                +"TODO"
            }
        }
    override val playPage: FC<Props>
        get() = externalCanvas("Labyrinth")
}