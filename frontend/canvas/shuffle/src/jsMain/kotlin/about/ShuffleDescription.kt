package about

import Deck
import props.list
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface ShuffleDescriptionProps : Props {
    var outShuffle: Boolean
    var text: String
}

val ShuffleExample = FC<ShuffleDescriptionProps> { props ->
    val deck = Deck(4, props.outShuffle)
    val deck2 = Deck(4, props.outShuffle)
    val deck3 = Deck(4, props.outShuffle)
    deck2.pile(2)
    deck3.pile(3)
    ReactHTML.p {
        +"$deck with 2-pile shuffle => ${deck.pilesString(2)} => $deck2"
    }
    ReactHTML.p {
        +props.text
    }
    ReactHTML.p {
        +"$deck with 3-pile shuffle => ${deck.pilesString(3)} => $deck3"
    }
}
//TODO link Farro Shuffle Wikipedia
val ShuffleDescription = FC<ShuffleDescriptionProps> { props ->
    ReactHTML.details {
        ReactHTML.summary {
            +"k-Pile ${if (props.outShuffle) "Out" else "In"}-Shuffling Explanation"
        }
        list {
            texts = listOf(
                "Take n cards and reorganize them into k piles by",
                "First card on first pile, second card on second pile, ...",
                "k'th card on k'th pile, k+1'th card on 1st pile, ...",
                "Once all cards have been put into piles put those on top of each other."
            ) + if (props.outShuffle) {
                "First pile on the bottom, second pile on first pile, ..."
            } else {
                "Last pile on the bottom, k-1'th pile on last pile, ..."
            }
        }
        if (props.outShuffle) {
            ShuffleExample {
                outShuffle = props.outShuffle
                text = "Doing a 2-pile shuffle again would loop back to the starting order."
            }
            ReactHTML.p {
                +"Note that the first card always stays the same."
            }
        } else {
            ShuffleExample {
                outShuffle = props.outShuffle
                text = "Doing a 2-pile shuffle again would reverse the starting order."
            }
        }
    }
    ReactHTML.details {
        ReactHTML.summary {
            +"Notation"
        }
        list {
            texts = listOf(
                "\"n:k = x\" means n cards loop back after x times repeatedly using k pile shuffles",
                "\"n:(2->3->4->5)\" = n cards using different k values"
            )
        }
        ReactHTML.p {
            +"Cases that I find interesting are those where adding more pile shuffles leads to more order."
        }
        ReactHTML.p {
            +"For example 99:(3-5), 99:(3-7) and 99:(5-7) all look more 'random' than 99:(3-5-7)"
        }
    }
    ReactHTML.details {
        ReactHTML.summary {
            +"Trivial Loops"
        }
        ReactHTML.p {
            +"Note that the following only applies to Out-Shuffling"
        }
        ReactHTML.p {
            +"k = sqrt(n) always takes 2 repetitions to loop back, e.g. 36:6 or 100:10"
        }
        ReactHTML.p {
            +"Simple math rules apply to those loops, e.g. 100:(10->10) == 100:(2->5->10) == 100:(2->5->2->5) == 100:(4->5->5) == 100:(5->20) == 100:(4->25)"
        }
        ReactHTML.p {
            +"Order doesn't matter, e.g. 100:(4->5->5) == 100:(5->4->5) == 100:(5->5->4)"
        }
    }
    ReactHTML.details {
        ReactHTML.summary {
            +"Non-Trivial Loops"
        }
        ReactHTML.p {
            +"Note that the following only applies to Out-Shuffling"
        }
        ReactHTML.p {
            +"Sometimes loops finish after a couple of repetitions, sometimes after hundreds, and sometimes after millions."
        }
        ReactHTML.p {
            +"My intuition would have assumed that for a given n the k with the largest loop would be related to prime numbers."
        }
        ReactHTML.p {
            +"But then there's cases like 80:44 = 86940, 99:22 = 925680, 100:48 = 429660, 123:15 = 19920600 and 140:122 = 29350552"
        }
        ReactHTML.p {
            +"Is it chaotic? Can we know which k would give the largest result for a given n? Can we know how long it would run?"
        }
    }
}