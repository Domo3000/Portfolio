package aboutpage.pages

import aboutme.AboutMeMenu
import aboutpage.AboutPageMenu
import css.Classes
import csstype.FontWeight
import emotion.react.css
import menu.SubMenu
import projects.ProjectsMenu
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
    var page: SubMenu
}

private val list: FC<ListProps>
    get() = FC { props ->
        props.page.elements
        if (props.page.elements.isNotEmpty()) {
            ul {
                props.page.elements.forEach {
                    li {
                        a {
                            href = "/${props.page.path}/${it.path}"
                            +it.text
                        }
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
                page = AboutMeMenu
            }
        }
        details {
            summary {
                +"About this page"
            }
            +"Information about this page"
            list {
                page = AboutPageMenu
            }
        }
        details {
            summary {
                +"Projects"
            }
            +"Little showcases"
            list {
                page = ProjectsMenu
            }
        }
    }
}