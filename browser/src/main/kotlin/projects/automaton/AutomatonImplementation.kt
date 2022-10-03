package projects.automaton

import csstype.FontWeight
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML

val Implementation = FC<Props> {
    ReactHTML.div {
        
        ReactHTML.p {
            css {
                fontWeight = FontWeight.bold
            }
            +"Binary Magic"
        }
        ReactHTML.p {
            +"Rules are stored as integers, and each active bit corresponds to one of the 8 configurations"
        }
        ReactHTML.p {
            +"(111)2 = 7 => flip 7th bit in rule"
        }
    }
}