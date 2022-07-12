package projects.shuffle

import Classnames
import projects.ProjectOverview
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

object ShuffleOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                +"TODO explanation of Pseudo Shuffling"
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
        get() = Canvas
}