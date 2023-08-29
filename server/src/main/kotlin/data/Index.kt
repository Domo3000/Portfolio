package data

import kotlinx.html.*

fun HTML.index() {
    val version = "0.5.3"
    head {
        title("Domo")
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1.0"
        }
        link {
            rel = "stylesheet"
            href = "/static/styles.css"
        }
        link {
            rel = "icon"
            type = "image/png"
            sizes = "16x16"
            href = "/static/favicon-16x16.png"
        }
        link {
            rel = "icon"
            type = "image/png"
            sizes = "32x32"
            href = "/static/favicon-32x32.png"
        }
    }
    body {
        div {
            id = "script-holder"
            script(src = "/static/main-$version.js") { }
        }
        script(src = "/static/automaton-$version.js") { async = true }
        script(src = "/static/kdtree-$version.js") { async = true }
        script(src = "/static/shuffle-$version.js") { async = true }
        script(src = "/static/labyrinth-$version.js") { async = true }
        script(src = "/static/connect4-$version.js") { async = true }
    }
}