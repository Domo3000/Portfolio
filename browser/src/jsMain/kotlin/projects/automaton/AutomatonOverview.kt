package projects.automaton

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props

object AutomatonOverview : ProjectOverview() {
    override val header = "Cellular Automaton"
    override val aboutPage: FC<Props>
        get() = About
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props>
        get() = externalCanvas("Automaton")
}