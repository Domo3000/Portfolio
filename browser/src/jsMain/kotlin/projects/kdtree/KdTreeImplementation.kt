package projects.kdtree

import react.FC
import react.Props
import web.window.WindowTarget
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.summary
import react.dom.html.ReactHTML.ul

val Implementation = FC<Props> {
    div {
        p {
            +"This was the first project I made for this page."
        }
        ReactHTML.p {
            ReactHTML.a {
                href = "https://github.com/Domo3000/Portfolio/tree/main/canvas/kdtree/src/main/kotlin"
                target = WindowTarget._blank
                +"Source Code"
            }
        }
        p {
            +"The implementation included:"
        }
        details {
            summary {
                +"Playing around with "
                a {
                    href = "https://www.w3schools.com/html/html5_canvas.asp"
                    target = WindowTarget._blank
                    +"Canvas"
                }
            }
            p {
                +"Coming from a mostly Backend background it was interesting to see how far HTML and JavaScript have developed."
            }
            p {
                +"Tinkering around a bit helped me to figure out how to set the viewport, handle inputs, redraw on state change."
            }
        }
        details {
            summary {
                +"Groundwork for all projects"
            }
            ul {
                li {
                    +"Play/About/Implementation layout "
                    a {
                        href =
                            "https://github.com/Domo3000/Portfolio/blob/main/browser/src/main/kotlin/projects/ProjectOverview.kt"
                        +"abstraction"
                    }
                    +" to be used for all projects"
                }
                li {
                    +"Helper functions for "
                    a {
                        href =
                            "https://github.com/Domo3000/Portfolio/blob/main/canvas/src/main/kotlin/canvas/utils.kt"
                        +"Canvas Elements"
                    }
                }
            }
        }
        details {
            summary {
                +"Implementing a "
                a {
                    href = "https://en.wikipedia.org/wiki/K-d_tree"
                    target = WindowTarget._blank
                    +"2D k-d Tree"
                }
            }
            p {
                +"A bit more advanced than a simple binary tree, but nothing special to mention here, except that it was fun figuring out how to randomly generate colors that don't change on every redraw."
            }
        }
    }
}
