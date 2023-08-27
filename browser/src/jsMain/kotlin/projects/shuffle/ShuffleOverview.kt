package projects.shuffle

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props

object ShuffleOverview : ProjectOverview() {
    override val header: String = "Pile-Shuffling Loops"
    override val aboutPage: FC<Props>
        get() = externalCanvas("ShuffleAbout")
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props> = externalCanvas("Shuffle")
}