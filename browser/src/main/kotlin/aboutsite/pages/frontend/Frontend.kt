package aboutsite.pages.frontend

import csstype.Clear
import csstype.FontStyle
import emotion.react.css
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.summary

val FrontendComponent = FC<Props> {
    div {
        h3 {
            a {
                href = "https://kotlinlang.org/docs/js-overview.html"
                +"Kotlin/JS"
            }
        }

        p {
            +"The frontent uses the "
            a {
                href = "https://github.com/JetBrains/kotlin-wrappers"
                +"React Wrapper"
            }
            +" and the main entry point is the "
            a {
                href = "https://github.com/Domo3000/Portfolio/tree/main/browser"
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
                href = "https://github.com/Domo3000/Portfolio/blob/main/canvas/src/main/kotlin/react/ExternalCanvas.kt"
                +"ExternalCanvas"
            }
            +" class sets up EventListeners. The Events get dispatched by "
            a {
                href =
                    "https://github.com/Domo3000/Portfolio/blob/main/browser/src/main/kotlin/projects/ProjectOverview.kt"
                +"ProjectOverview.externalCanvas(name: String)"
            }
        }

        h3 {
            a {
                href = "https://reactjs.org/"
                +"React"
            }
        }

        details {
            summary {
                +"State and References"
            }

            p {
                +"TODO rework this whole page" // TODO rework this whole page
            }

            p {
                +"The following is an example of how different ways of storing values changes their behaviour."
            }

            p {
                +"The canvas on the left gets redrawn on typing, clicking or resizing. The texts on the right get updated if React notices a State change."
            }

            p {
                +"Try typing anything, clicking on the canvas and resizing the window."
            }

            div {
                externalCanvas("Debug")()
            }

            div {
                css {
                    clear = Clear.left
                }
                p {
                    +"As we can see only the "
                    span {
                        css {
                            fontStyle = FontStyle.italic
                        }
                        +"stringHolderState"
                    }
                    +", "
                    span {
                        css {
                            fontStyle = FontStyle.italic
                        }
                        +"mutableListState"
                    }
                    +" and "
                    span {
                        css {
                            fontStyle = FontStyle.italic
                        }
                        +"pressesHolderState"
                    }
                    +" act how we would expect."
                }

                p {
                    +"That's because for them the reference to the Holder object stays the same and only the values inside change."
                }

                p {
                    +""
                }
            }

        }
    }
}