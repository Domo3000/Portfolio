package aboutpage.pages

import aboutme.AboutMeStates
import aboutpage.AboutPageStates
import css.Classes
import csstype.FontWeight
import emotion.react.css
import menu.SubmenuState
import projects.ProjectStates
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.summary
import react.dom.html.ReactHTML.ul

external interface ListProps : Props {
    var elements: List<SubmenuState>
}

private val list: FC<ListProps>
    get() = FC { props ->
        if (props.elements.isNotEmpty()) {
            ul {
                props.elements.forEach {
                    li {
                        +it.text
                        // TODO onClick change to state
                    }
                }
            }
        }
    }

val IntroComponent = FC<Props> {
    div {
        css(Classes.text)

        p {
            +"This is a full-stack web application written in "
            a {
                href = "https://kotlinlang.org/docs/multiplatform.html#full-stack-web-applications"
                +"Kotlin"
            }
            +"."
        }

        p {
            +"I'm using the "
            a {
                href = "https://github.com/johnrengelman/shadow"
                +"Shadow Plugin"
            }
            +" to create a Fat JAR."
        }

        p {
            +"Source code is available here: "
            a {
                href = "https://github.com/Domo3000/Portfolio"
                +"Github"
            }
        }

        p {
            css {
                fontWeight = FontWeight.bold
            }
            +"Pages:"
        }

        details {
            summary {
                +"About Me"
            }
            +"Information about the author"
            list {
                elements = AboutMeStates.states
            }
        }
        details {
            summary {
                +"About this page"
            }
            +"Information about this page"
            list {
                elements = AboutPageStates.states
            }
        }
        details {
            summary {
                +"Projects"
            }
            +"Little showcases"
            list {
                elements = ProjectStates.states
            }
        }
    }
}