package overview

import aboutme.AboutMeMenu
import aboutsite.AboutSiteMenu
import menu.RecursiveSubMenu
import menu.SubMenu
import projects.ProjectsMenu
import react.FC
import react.Props
import react.createElement
import react.router.IndexRoute
import react.router.PathRoute
import react.router.Routes
import react.router.dom.BrowserRouter

val Routing = FC<Props> {
    val routes = listOf(
        AboutMeMenu.routes(),
        AboutSiteMenu.routes(),
        ProjectsMenu.routes()
    ).flatten()

    BrowserRouter {
        Routes {
            IndexRoute {
                index = true
                element = createElement(overview())
            }
            routes.forEach { (route, state) ->
                PathRoute {
                    path = route
                    element = createElement(overview(state))
                }
            }
            PathRoute {
                path = "*"
                element = createElement(overview(NotFoundState))
            }
        }
    }
}

private fun SubMenu.routes() = elements.map { "/$path/${it.path}" to it }

private fun RecursiveSubMenu.routes(parentPath: String = ""): List<Pair<String, OverviewState>> {
    val fullPath = "$parentPath/$path"

    val self = state?.let { fullPath to it }?.let { listOf(it) } ?: emptyList()
    val children = elements.flatMap { it.routes(fullPath) }

    return self + children
}

/*
private fun ProjectsMenu.subRoutes() {
    ProjectStates.states.flatMap { projectState ->
        ProjectSubState.entries.toList().map { subState ->
            val state = projectState(ProjectStateData(subState, path))

            "${state.route}/${subState.toString().lowercase()}" to state
        }

    }
}

 */