package projects

import menu.SubMenu
import menu.SubmenuState
import projects.automaton.AutomatonOverview
import projects.connect4.Connect4Overview
import projects.kdtree.KdTreeOverview
import projects.shuffle.ShuffleOverview
import react.FC
import react.Props

sealed interface ProjectState : SubmenuState

object ProjectStates {
    object KdTree : ProjectState {
        override val text: String = "KdTree"
        override val component: FC<Props>
            get() = KdTreeOverview.create
    }
    object Automaton: ProjectState {
        override val text: String = "Automaton"
        override val component: FC<Props>
            get() = AutomatonOverview.create
    }
    object Shuffle: ProjectState {
        override val text: String = "\"Shuffling\""
        override val component: FC<Props>
            get() = ShuffleOverview.create
    }
    object Connect4: ProjectState {
        override val text: String = "Connect4"
        override val component: FC<Props>
            get() = Connect4Overview
    }

    val states = listOf(KdTree, Automaton, Shuffle, Connect4)
}

object ProjectsMenu : SubMenu<ProjectState>(ProjectStates.KdTree) {
    override val text: String = "Projects"
    override val matchingState = ProjectState::class
    override val elements: List<SubmenuState> = ProjectStates.states
}