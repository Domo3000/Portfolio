package projects

import menu.RecursiveSubMenu
import overview.OverviewState
import projects.automaton.AutomatonOverview
import projects.connect4.Connect4Overview
import projects.kdtree.KdTreeOverview
import projects.labyrinth.LabyrinthOverview
import projects.shuffle.ShuffleOverview
import projects.trippy.TrippyOverview
import react.FC
import react.Props

//sealed interface ProjectState : OverviewState

data class ProjectState(
    val projectOverview: ProjectOverview,
    val state: ProjectSubState,
    val route: String
) : OverviewState {
    override val component: FC<Props>
        get() = FC<Props> {
            projectOverview.create {
                subState = state
                parentRoute = route
            }
        }
}

class ProjectSubStateMenu(
    projectOverview: ProjectOverview,
    subState: ProjectSubState,
    override val parentPath: String
) : RecursiveSubMenu() {
    override val text: String = subState.toString()
    override val path: String = text.lowercase()
    override val elements: List<RecursiveSubMenu> = emptyList()

    override val state: OverviewState = ProjectState(
        projectOverview,
        subState,
        path
    )
}

class ProjectSubMenu(
    override val text: String,
    projectOverview: ProjectOverview,
    override val path: String = text.lowercase(),
    val externalName: String = text
) : RecursiveSubMenu() {
    override val parentPath = ProjectsMenu.fullPath

    override val elements: List<RecursiveSubMenu> = ProjectSubState.entries.toList().map { subState ->
        ProjectSubStateMenu(
            projectOverview,
            subState,
            fullPath
        )
    }

    override val state: OverviewState = elements.first().state!!
}

object ProjectStates {
    private val KdTree: ProjectSubMenu =
        ProjectSubMenu(
            text = "KdTree",
            projectOverview = KdTreeOverview
        )

    private val Automaton: ProjectSubMenu =
        ProjectSubMenu(
            text = "Automaton",
            projectOverview = AutomatonOverview
        )

    private val Trippy: ProjectSubMenu =
        ProjectSubMenu(
            text = "Trippy",
            projectOverview = TrippyOverview
        )

    private val Shuffle: ProjectSubMenu =
        ProjectSubMenu(
            text = "\"Shuffling\"",
            projectOverview = ShuffleOverview,
            path = "shuffling",
            externalName = "Shuffle"
        )

    private val Labyrinth: ProjectSubMenu =
        ProjectSubMenu(
            text = "Labyrinth",
            projectOverview = LabyrinthOverview
        )

    val Connect4: ProjectSubMenu =
        ProjectSubMenu(
            text = "Connect4",
            projectOverview = Connect4Overview,
            path = "connect-four"
        )

    val states = listOf(KdTree, Automaton, Trippy, Shuffle, Labyrinth, Connect4)
}

object ProjectsMenu : RecursiveSubMenu() {
    override val text: String = "Projects"
    override val path: String = text.lowercase()
    override val elements: List<RecursiveSubMenu> = ProjectStates.states
}

// TODO fix issues: name, buttons should be a, menu not black if in /about route