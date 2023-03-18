package projects.connect4

import react.FC
import react.Props
import react.dom.html.ReactHTML

val About = FC<Props> {
    ReactHTML.div {
        ReactHTML.strong {
            +"Connect4"
        }
        ReactHTML.div {
            +"I thought it would be appropriate to use Websockets to connect to the Backend."
        }
        ReactHTML.div {
            +"In the Backend it will run various algorithms to determine the next move."
        }
        ReactHTML.div {
            +"TODO explain MonteCarlo, NeuralNetwork, Length algorithms."
        }
    }
}