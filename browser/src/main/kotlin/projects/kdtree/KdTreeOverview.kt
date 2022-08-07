package projects.kdtree

import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props

object KdTreeOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = About
    override val implementationPage: FC<Props>
        get() = Implementation
    override val playPage: FC<Props>
        get() = externalCanvas("KdTree")
}