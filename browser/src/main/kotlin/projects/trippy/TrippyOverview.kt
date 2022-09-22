package projects.trippy

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML

object TrippyOverview : ProjectOverview() {
    override val header = "Trippy"
    override val aboutPage: FC<Props>
        get() = externalCanvas("TrippyAbout")
    override val implementationPage: FC<Props>
        get() = FC {
            ReactHTML.div {
                +"TODO"
            }
        }
    override val playPage: FC<Props>
        get() = externalCanvas("Trippy")
}