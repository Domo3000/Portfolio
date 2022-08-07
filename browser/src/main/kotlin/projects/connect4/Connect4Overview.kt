package projects.connect4

import Classnames
import kotlinx.browser.document
import org.w3c.dom.events.Event
import projects.ProjectOverview
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.useEffectOnce

object Connect4Overview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                p {
                    +"This project has not been implemented yet!"
                }
                p {
                    +"TODO explanation of Connect4 and Webhooks"
                }
            }
        }
    override val implementationPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                p {
                    +"This project has not been implemented yet!"
                }
                p {
                    +"For now it's just used to show one way how to connect a Canvas Element from another Javascript file to this React App."
                }
                p {
                    +"The JS file starts listening to an Event to hydrate the root with it's own Canvas Element once we are on this page."
                }
                p {
                    +"I felt it fitting to have Connect4 connect to the Backend via Webhooks, but that's not yet fully implemented!"
                }
            }
        }
    override val playPage: FC<Props>
        get() = FC {
            p {
                className = Classnames.text
                +"TODO"
            }

            div {
                id = "external-holder"
            }

            useEffectOnce {
                document.dispatchEvent(Event("Connect4"))

                cleanup {
                    document.dispatchEvent(Event("Connect4Cleanup"))
                }
            }
        }
}