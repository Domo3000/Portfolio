package projects.connect4

import react.FC
import react.Props
import react.dom.html.ReactHTML

val About = FC<Props> {
    ReactHTML.div {
        ReactHTML.strong {
            +"Connect Four"
        }
        ReactHTML.p {
            +"I thought it would be appropriate to use Websockets to connect to the Backend."
        }
        ReactHTML.p {
            +"The Backend will run various algorithms to determine the next move."
        }
        ReactHTML.strong {
            +"Algorithms"
        }
        ReactHTML.details {
            ReactHTML.summary {
                +"Length Measuring"
            }
            ReactHTML.p {
                ReactHTML.a {
                    href = "https://github.com/Domo3000/Portfolio/blob/main/server/src/main/kotlin/connect4/ai/length/LengthAI.kt"
                    +"Source Code"
                }
            }
            ReactHTML.p {
                +"This is a simple algorithm that measures what's longest adjacent pieces for each player that can be created or prevented with the next move."
            }
        }
        ReactHTML.details {
            ReactHTML.summary {
                +"Monte Carlo"
            }
            ReactHTML.p {
                ReactHTML.a {
                    href = "https://github.com/Domo3000/Portfolio/blob/main/server/src/main/kotlin/connect4/ai/monte/MonteCarloAI.kt"
                    +"Source Code"
                }
            }
            ReactHTML.p {
                +"This is a little bit more complex algorithm that randomly plays game for each available position."
            }
            ReactHTML.p {
                +"It counts the number of winning and losing games and picks the position with the best ratio."
            }
        }
        ReactHTML.details {
            ReactHTML.summary {
                +"Neural Network"
            }
            ReactHTML.p {
                ReactHTML.a {
                    href = "https://github.com/Domo3000/Portfolio/tree/main/server/src/main/kotlin/connect4/ai/neural"
                    +"Source Code"
                }
            }
            ReactHTML.p {
                +"This uses pre-trained networks that learned how to play the game."
            }
        }
    }
}