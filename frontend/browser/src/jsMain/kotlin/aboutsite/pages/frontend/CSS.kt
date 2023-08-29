package aboutsite.pages.frontend

import props.*
import react.*
import web.window.WindowTarget
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import utils.Github

val CSSComponent = FC<Props> {
    div {
        h3 {
            +"External CSS"
        }

        p {
            +"The "
            a {
                href = Github.link("blob/main/server/src/main/kotlin/Application.kt")
                target = WindowTarget._blank
                +"Backend server"
            }
            +" generates the "
            a {
                href = Github.link("blob/main/server/src/main/kotlin/data/Styles.kt")
                target = WindowTarget._blank
                +"styles.css"
            }
            +" file using the "
            a {
                href = "https://ktor.io/docs/css-dsl.html"
                target = WindowTarget._blank
                +"CSS DSL"
            }
            +"."
        }

        formattedCode {
            lines = listOf(
                // TODO create Kotlin DSL for this
                CodeLine(
                    listOf(
                        "media(".white(),
                        "\"only screen and (min-width: 800px)\"".green(),
                        ") {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\trule(".white(),
                        "\".phone-element\"".green(),
                        ") {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t\tdisplay".purple(),
                        " = Display.".white(),
                        "none".purple()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t}".white(),
                    )
                ),
                CodeLine(
                    listOf(
                        "}".white(),
                    )
                )
            )
        }

        p {
            +"This is mostly used for Media Queries on "
            a {
                href = Github.link("blob/main/frontend/src/jsMain/kotlin/css/ClassNames.kt")
                target = WindowTarget._blank
                +"ClassNames"
            }
            +"."
        }

        h3 {
            +"Inline CSS"
        }

        p {
            +"The Frontend uses the "
            a {
                href = "https://emotion.sh/docs/introduction"
                target = WindowTarget._blank
                +"Emotion library"
            }
            +" via the "
            a {
                href = "https://github.com/JetBrains/kotlin-wrappers/tree/master/kotlin-emotion"
                target = WindowTarget._blank
                +"Kotlin Wrapper"
            }
            +"."
        }

        p {
            +"Technically it is Internal CSS as it gets added to the <style> section in the <head> of the document, but the implementation feels like using Inline CSS:"
        }

        formattedCode {
            lines = listOf(
                // TODO Kotlin DSL for this
                CodeLine(
                    listOf(
                        "ReactHTML.".white(),
                        "p".purple(),
                        " {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\tcss".yellow(),
                        " {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t\tbackgroundColor".purple(),
                        " = NamedColor.".white(),
                        "black".purple(),
                    )
                ),
                CodeLine(
                    listOf(
                        "\t}".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t+".white(),
                        "\"text\"".green()
                    )
                ),
                CodeLine(
                    listOf(
                        "}".white()
                    )
                )
            )
        }

        p {
            +"We can also combine it with the ClassNames from the External CSS:"
        }

        formattedCode {
            lines = listOf(
                // TODO Kotlin DSL for this
                CodeLine(
                    listOf(
                        "ReactHTML.".white(),
                        "div".purple(),
                        " {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\tcss".yellow(),
                        "(ClassNames.phoneElement ".white(),
                        "and ".yellow(),
                        "\"menu\"".green(),
                        ") {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t\tzIndex".purple(),
                        " = integer(".white(),
                        "5".blue(),
                        ")".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t\ttop".purple(),
                        " = ".white(),
                        "40".blue(),
                        ".".white(),
                        "px".purple()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t}".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "}".white()
                    )
                )
            )
        }

        p {
            a {
                href = Github.link("blob/main/frontend/src/jsMain/kotlin/css/Classes.kt")
                target = WindowTarget._blank
                +"Styles"
            }
            +" can also be stored"
        }

        formattedCode {
            lines = listOf(
                // TODO Kotlin DSL for this
                CodeLine(
                    listOf(
                        "val".orange(),
                        " centered".purple(),
                        ": CSS = {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\tmargin".purple(),
                        " = Auto.".white(),
                        "auto".purple()
                    )
                ),
                CodeLine(
                    listOf(
                        "}".white()
                    )
                )
            )
        }

        p {
            +"and reused"
        }

        formattedCode {
            lines = listOf(
                // TODO Kotlin DSL for this
                CodeLine(
                    listOf(
                        "ReactHTML.".white(),
                        "h6".purple(),
                        " {".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\tcss".yellow(),
                        "(Classes.".white(),
                        "centered".purple(),
                        ")".white()
                    )
                ),
                CodeLine(
                    listOf(
                        "\t+".white(),
                        "\"Impressum\"".green()
                    )
                ),
                CodeLine(
                    listOf(
                        "}".white()
                    )
                )
            )
        }
    }
}