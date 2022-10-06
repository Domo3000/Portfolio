package projects.debug

import csstype.*
import emotion.react.css
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML

val DebugPage = FC<Props> {
    ReactHTML.div {
        ReactHTML.h2 {
            css {
                textAlign = TextAlign.center
            }
            +"Debug"
        }
    }

    ReactHTML.div {
        ReactHTML.p {
            +"TODO rework this whole page" // TODO rework this whole page
        }

        ReactHTML.p {
            +"The following is an example of how different ways of storing values changes their behaviour."
        }

        ReactHTML.p {
            +"The canvas on the left gets redrawn on typing, clicking or resizing. The texts on the right get updated if React notices a State change."
        }

        ReactHTML.p {
            +"Try typing anything, clicking on the canvas and resizing the window."
        }

        ReactHTML.div {
            externalCanvas("Debug")()
        }

        ReactHTML.div {
            css {
                clear = Clear.left
            }
            ReactHTML.p {
                +"As we can see only the "
                ReactHTML.span {
                    css {
                        fontStyle = FontStyle.italic
                    }
                    +"stringHolderState"
                }
                +", "
                ReactHTML.span {
                    css {
                        fontStyle = FontStyle.italic
                    }
                    +"mutableListState"
                }
                +" and "
                ReactHTML.span {
                    css {
                        fontStyle = FontStyle.italic
                    }
                    +"pressesHolderState"
                }
                +" act how we would expect."
            }

            ReactHTML.p {
                +"That's because for them the reference to the Holder object stays the same and only the values inside change."
            }

            ReactHTML.p {
                +""
            }
        }
    }
}