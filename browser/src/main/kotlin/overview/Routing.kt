package overview

import aboutme.AboutMeMenu
import aboutsite.AboutSiteMenu
import menu.SubMenu
import projects.ProjectsMenu
import react.FC
import react.Props
import react.createElement
import react.router.Route
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
            Route {
                index = true
                element = createElement(overview())
            }
            routes.forEach { (route, state) ->
                Route {
                    path = route
                    element = createElement(overview(state))
                }
            }
            Route {
                path = "*"
                element = createElement(overview(NotFoundState))
            }
        }
    }
}

private fun SubMenu.routes() = elements.map { "/$path/${it.path}" to it }