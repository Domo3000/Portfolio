package aboutsite.pages

import aboutme.AboutMeMenu
import aboutsite.AboutSiteMenu
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
import react.dom.html.ReactHTML.strong
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
        p {
            +"This is a full-stack web application written in "
            a {
                href = "https://kotlinlang.org/docs/multiplatform.html#full-stack-web-applications"
                +"Kotlin"
            }
            +"."
        }

        p {
            +"Source code is available on "
            a {
                href = "https://github.com/Domo3000/Portfolio"
                +"Github"
            }
            +", but this site is also trying to be self-documenting."
        }

        strong { // TODO strong, h4 or something, follow some styleguide
            +"Pages:"
        }

        details {
            summary {
                +AboutMeMenu.text
            }
            +"Information about the author"
            list {
                page = AboutMeMenu
            }
        }
        details {
            summary {
                +AboutSiteMenu.text
            }
            +"Information about this site"
            list {
                page = AboutSiteMenu
            }
        }
        details {
            summary {
                +ProjectsMenu.text
            }
            +"Little showcases"
            list {
                page = ProjectsMenu
            }
        }
    }
}