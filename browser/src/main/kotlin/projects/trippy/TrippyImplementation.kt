package projects.trippy


import react.FC
import react.Props
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.summary

val Implementation = FC<Props> {
    div {
        +"TODO talk about optimization attempts"
    }
    details {
        summary {
            +"Available Sizes"
        }
        p {
            +"In order to increase speed I'm only using Integer values for drawing the squares."
        }
        p {
            +"As I'm using a Canvas with a resolution of 800x600 I can only use sizes that are a common factor of both of them."
        }
        p {
            +"E.g. 800 : 2 = 400 and 600 : 2 = 300"
        }
        p {
            +"TODO datalist"
        }
    }
}