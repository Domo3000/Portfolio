package projects.shuffle

import Classnames
import projects.ProjectOverview
import projects.externalCanvas
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

object ShuffleOverview : ProjectOverview() {
    override val aboutPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                p {
                    +"TODO explanation of Pile-Shuffling"
                }
                p {
                    +"First card on first pile, second card on second pile, k'th card on k'th pile, k+1'th card on 1st pile, etc"
                }
                p {
                    +"Then put piles back on top of each other"
                }
                p {
                    +"Deck [1, 2, 3, 4] with 2-Pile Shuffle => [1, 3], [2, 4] => [1, 3, 2, 4]"
                }
                p {
                    +"Cycles: e.g. size = 100 => 10 -> 10 or 4 -> 5 -> 5 or 5 -> 20 or 4 -> 25"
                }
                p {
                    +"In MTG whenever I created a new Commander deck I used to do 3-5-7 to introduce some initial \"randomness\" to the 99, which actually looks less \"random\" than 3-5, 5-7 or 3-7"
                }
                p {
                    +"Which configurations cause the longest loop?"
                }
            }
        }
    override val implementationPage: FC<Props>
        get() = FC {
            div {
                className = Classnames.text
                p {
                    +"TODO explanation of Code"
                }
                p {
                    +"Creating abstract ExternalCanvas class to move project code easier into own modules"
                }
            }
        }
    override val playPage: FC<Props> = externalCanvas("Shuffle")
}