package aboutme.information

import react.FC
import react.Props
import web.window.WindowTarget
import react.dom.html.ReactHTML

val ProfessionalState = FC<Props> {
    // TODO use apache poi to generate CV PDF and put a download button here
    //val now = kotlinx.datetime.Clock.System.now()

    ReactHTML.p {
        +"I'm a software developer."
    }

    ReactHTML.p {
        +"Currently working as a consultant for "
        ReactHTML.a {
            href = "https://www.openvalue.eu/"
            target = WindowTarget._blank
            +"OpenValue"
        }
    }
}
