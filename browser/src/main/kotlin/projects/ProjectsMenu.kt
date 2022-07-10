package projects

import aboutme.AboutMeStates
import menu.SubMenu
import menu.SubmenuState
import projects.automaton.AutomatonOverview
import projects.connect4.Connect4Overview
import projects.kdtree.KdTreeOverview
import react.FC
import react.Props

sealed interface ProjectState : SubmenuState

object ProjectStates {
    object KdTree : ProjectState {
        override val text: String = "KdTree"
        override val component: FC<Props>
            get() = KdTreeOverview
    }
    object Automaton: ProjectState {
        override val text: String = "Automaton"
        override val component: FC<Props>
            get() = AutomatonOverview
    }
    object Connect4: ProjectState {
        override val text: String = "Connect4"
        override val component: FC<Props>
            get() = Connect4Overview
    }

    val states = listOf(KdTree, Automaton, Connect4)
}

object ProjectsMenu : SubMenu<ProjectState>(ProjectStates.KdTree) {
    override val text: String = "Projects"
    override val matchingState = ProjectState::class
    override val elements: List<SubmenuState> = ProjectStates.states
}