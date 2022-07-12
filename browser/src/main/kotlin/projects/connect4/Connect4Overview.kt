package projects.connect4

import Classnames
import kotlinx.browser.document
import org.w3c.dom.events.Event
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.useEffect

val Connect4Overview = FC<Props> {
    p {
        className = Classnames.text
        +"TODO"
    }

    div {
        id = "external-holder"
    }

    useEffect {
        document.dispatchEvent(Event("Connect4"))
    }
}