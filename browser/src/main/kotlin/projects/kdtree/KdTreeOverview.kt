package projects.kdtree

import Classnames
import projects.ProjectOverview
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

object KdTreeOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                +"TODO explanation of KdTrees and Mondrian"
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
        get() = FC {
            Canvas { tree = null } // TODO tree as state instead of props
        }
}