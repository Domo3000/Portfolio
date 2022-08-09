package projects.automaton

import Classnames
import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

object AutomatonOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                +"TODO explanation of Automaton"
            }
        }
    override val implementationPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                +"TODO explanation of Code"
            }
        }
    override val playPage: FC<Props>
        get() = externalCanvas("Automaton")
}