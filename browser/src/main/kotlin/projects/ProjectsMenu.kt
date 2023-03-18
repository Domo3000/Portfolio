package projects

import menu.SubMenu
import menu.SubmenuState
import projects.automaton.AutomatonOverview
import projects.connect4.Connect4Overview
import projects.debug.DebugPage
import projects.kdtree.KdTreeOverview
import projects.labyrinth.LabyrinthOverview
import projects.shuffle.ShuffleOverview
import projects.trippy.TrippyOverview
import react.FC
import react.Props

sealed interface ProjectState : SubmenuState

sealed interface ExternalProjectState : SubmenuState {
    val externalName: String
}

object ProjectStates {
    object KdTree : ExternalProjectState {
        override val text: String = "KdTree"
        override val path: String = "kd-tree"
        override val externalName = text
        override val component: FC<Props>
            get() = KdTreeOverview.create
    }
    object Automaton: ExternalProjectState {
        override val text: String = "Automaton"
        override val path: String = text.lowercase()
        override val externalName = text
        override val component: FC<Props>
            get() = AutomatonOverview.create
    }
    object Trippy: ExternalProjectState {
        override val text: String = "Trippy"
        override val path: String = text.lowercase()
        override val externalName = text
        override val component: FC<Props>
            get() = TrippyOverview.create
    }
    object Shuffle: ExternalProjectState {
        override val text: String = "\"Shuffling\""
        override val path: String = "shuffling"
        override val externalName = "Shuffle"
        override val component: FC<Props>
            get() = ShuffleOverview.create
    }
    object Labyrinth: ExternalProjectState {
        override val text: String = "Labyrinth"
        override val path: String = text.lowercase()
        override val externalName = text
        override val component: FC<Props>
            get() = LabyrinthOverview.create
    }
    object Connect4: ExternalProjectState {
        override val text: String = "Connect4"
        override val path: String = "connect-four"
        override val externalName = text
        override val component: FC<Props>
            get() = Connect4Overview.create
    }
    object Debug: ExternalProjectState {
        override val text: String = "Debug"
        override val path: String = text.lowercase()
        override val externalName = text
        override val component: FC<Props>
            get() = DebugPage
    }

    val states = listOf(KdTree, Automaton, Trippy, Shuffle, Labyrinth, Connect4, Debug)
}

object ProjectsMenu : SubMenu(ProjectStates.KdTree) {
    override val text: String = "Projects"
    override val path: String = text.lowercase()
    override val matchingState = ProjectState::class
    override val elements: List<SubmenuState> = ProjectStates.states
}