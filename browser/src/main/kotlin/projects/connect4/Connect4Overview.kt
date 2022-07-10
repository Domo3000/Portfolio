package projects.connect4

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.events.addEventHandler
import org.w3c.dom.get
import projects.ProjectStates
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.script
import react.useEffect
import react.useEffectOnce
import kotlin.js.RegExp

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
