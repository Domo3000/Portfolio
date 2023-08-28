package projects.trippy

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props

object TrippyOverview : ProjectOverview() {
    override val header = "Trippy"
    override val aboutPage: FC<Props>
        get() = externalCanvas("TrippyAbout")
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props>
        get() = externalCanvas("Trippy")
}