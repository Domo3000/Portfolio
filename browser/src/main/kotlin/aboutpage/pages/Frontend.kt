package aboutpage.pages

import Classnames
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

val FrontendComponent = FC<Props> {
    div {
        className = Classnames.text

        p {
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
            +"In order to reduce size of the .js file I split some larger projects up into their own modules. Their implementation pages explain in more detail how they were included."
        }
    }
}