package aboutme.information

import csstype.ObjectFit
import csstype.pct
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.useState

sealed interface State

object States {
    object Professional: State
    object Casual: State
}

fun State.isProfessional() = (this == States.Professional)

fun State.switch() = when(this) {
    States.Professional -> States.Casual
    States.Casual -> States.Professional
}

val InformationComponent = FC<Props> {
    val (state, setState) = useState<State>(States.Professional)

    ReactHTML.div {
        ReactHTML.h3 {
            //css(Classes.centered)
            + if(state.isProfessional()) {
                "Dominik Leys"
            } else {
                "Domo"
            }
        }
        ReactHTML.div {
            ReactHTML.img {
                css {
                    width = 100.pct
                    height = 100.pct
                    maxWidth = 300.px
                    objectFit = ObjectFit.contain
                }
                src = "/static/" + if (state.isProfessional()) "dominik-professional.png" else "domo-casual.png"
                alt = "${if (state.isProfessional()) "professional" else "casual"} looking image"
                onClick = {
                    setState(state.switch())
                }
            }
        }

        ReactHTML.div {
            if(state.isProfessional()) {
                ReactHTML.p {
                    +"I'm a software developer."
                    /*
                        TODO
                        education
                        work history

                        would be nice to generate a downloadable CV as a PDF file
                     */
                }
                ReactHTML.p {
                    +"TODO"
                }
            } else {
                ReactHTML.p {
                    +"I'm a gamer."
                }
                ReactHTML.p {
                    +"TODO"
                }
            }
        }
    }
}

/*
    TODO general information, age, languages spoken, etc
 */