package data

import kotlinx.html.*

fun HTML.index() {
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
    }
    body {
        div {
            id = "script-holder"
            script(src = "/static/main.js") { }
        }
        script(src = "/static/automaton.js") { async = true }
        script(src = "/static/kdtree.js") { async = true }
        script(src = "/static/shuffle.js") { async = true }
        script(src = "/static/labyrinth.js") { async = true }
        script(src = "/static/connect4.js") { async = true }
    }
}