package overview

import aboutme.AboutMeMenu
import aboutsite.AboutSiteMenu
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