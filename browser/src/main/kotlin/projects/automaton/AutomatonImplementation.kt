package projects.automaton

import react.FC
import react.Props
import react.dom.html.ReactHTML

val Implementation = FC<Props> {
    ReactHTML.div {
        ReactHTML.strong {
            +"Binary Magic"
        }
        ReactHTML.p {
            ReactHTML.a {
                href = "https://github.com/Domo3000/Portfolio/tree/main/canvas/automaton/src/main/kotlin/automaton"
                +"Source Code"
            }
        }
        ReactHTML.p {
            +"Rules are stored as Integers, and each active bit corresponds to one of the 8 configuration options."
        }
        ReactHTML.p {
            +"No rules are active => 00000000 = Rule 0"
        }
        ReactHTML.p {
            +"e.g. activate the rule where all parent elements are black = (111)2 = 7 => 7th bit in ruleset (0 indexed) gets set to 1 => 1000000 = Rule 128"
        }
    }
}