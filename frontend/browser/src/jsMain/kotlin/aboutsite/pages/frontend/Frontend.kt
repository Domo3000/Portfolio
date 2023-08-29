package aboutsite.pages.frontend

import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import utils.Github
import web.window.WindowTarget

val FrontendComponent = FC<Props> {
    div {
        h3 {
            a {
                href = "https://kotlinlang.org/docs/js-overview.html"
                target = WindowTarget._blank
                +"Kotlin/JS"
            }
        }

        p {
            +"The frontent uses the "
            a {
                href = "https://github.com/JetBrains/kotlin-wrappers"
                target = WindowTarget._blank
                +"React Wrapper"
            }
            +" and the main entry point is the "
            a {
                href = Github.link("tree/main/frontend/browser")
                target = WindowTarget._blank
                +"browser"
            }
            +" module."
        }

        p {
            +"In order to reduce size of the .js file I split the projects up into their own modules."
        }

        p {
            +"To connect other .js files with this React App the "
            a {
                href = Github.link("blob/main/frontend/canvas/src/jsMain/kotlin/canvas/ExternalCanvas.kt")
                target = WindowTarget._blank
                +"ExternalCanvas"
            }
            +" class sets up EventListeners. The Events get dispatched by "
            a {
                href = Github.link("blob/main/frontend/browser/src/jsMain/kotlin/projects/ProjectOverview.kt")
                target = WindowTarget._blank
                +"ProjectOverview.externalCanvas(name: String)"
            }
            +"."
        }
        p {
            +"When one of the ExternalCanvas projects receives their Initialize Event they will use "
            a {
                href = "https://react.dev/reference/react-dom/client/hydrateRoot"
                target = WindowTarget._blank
                +"hydrateRoot()"
            }
            +" to display themselves."
        }
    }
}