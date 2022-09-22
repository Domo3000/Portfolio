package projects.automaton

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

object AutomatonOverview : ProjectOverview() {
    override val header = "Cellular Automaton"
    override val aboutPage: FC<Props>
        get() = FC {
            div {
                +"TODO explanation of Automaton"
            }
        }
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props>
        get() = externalCanvas("Automaton")
}