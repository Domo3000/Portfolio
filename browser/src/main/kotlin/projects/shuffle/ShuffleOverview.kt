package projects.shuffle

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

object ShuffleOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = externalCanvas("ShuffleAbout")
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props> = externalCanvas("Shuffle")
}