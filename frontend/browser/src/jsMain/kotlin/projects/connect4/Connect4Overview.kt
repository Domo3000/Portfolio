package projects.connect4

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props

object Connect4Overview : ProjectOverview() {
    override val header: String = "Connect Four"
    override val aboutPage: FC<Props>
        get() = About
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props>
        get() =  externalCanvas("Connect4")
}