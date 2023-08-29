package about

import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import web.cssom.pct

external interface HighestTableProps : Props {
    var inHighest: List<Pair<Position, ShuffleCounter>>
    var outHighest: List<Pair<Position, ShuffleCounter>>
}

val HighestTable = FC<HighestTableProps> { props ->
    ReactHTML.table {
        css {
            width = 100.pct
        }
        ReactHTML.thead {
            ReactHTML.tr {
                ReactHTML.th {
                    +"In-Shuffle"
                    colSpan = 2
                }
                ReactHTML.th {
                    +"Out-Shuffle"
                    colSpan = 2
                }
            }
            ReactHTML.tr {
                ReactHTML.th {
                    +"n:k"
                }
                ReactHTML.th {
                    +"Count"
                }
                ReactHTML.th {
                    +"n:k"
                }
                ReactHTML.th {
                    +"Count"
                }
            }
        }
        ReactHTML.tbody {
            props.inHighest.zip(props.outHighest).map { (inPair, outPair) ->
                ReactHTML.tr {
                    ReactHTML.th {
                        +inPair.first.toPrettyString()
                    }
                    ReactHTML.th {
                        +inPair.second.counter.toString()
                    }
                    ReactHTML.th {
                        +outPair.first.toPrettyString()
                    }
                    ReactHTML.th {
                        +outPair.second.counter.toString()
                    }
                }
            }
        }
    }
}
