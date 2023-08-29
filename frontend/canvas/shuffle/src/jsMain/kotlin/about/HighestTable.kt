package about

import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import web.cssom.pct

external interface HighestTableProps : Props {
    var outHighest: List<Pair<Position, ShuffleCounter>>
    var inHighest: List<Pair<Position, ShuffleCounter>>
}

val HighestTable = FC<HighestTableProps> { props ->
    ReactHTML.table {
        css {
            width = 100.pct
        }
        ReactHTML.thead {
            ReactHTML.tr {
                ReactHTML.th {
                    +"Out-Shuffle"
                    colSpan = 2
                }
                ReactHTML.th {
                    +"In-Shuffle"
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
            props.outHighest.zip(props.inHighest).map { (inPair, outPair) ->
                ReactHTML.tr {
                    ReactHTML.td {
                        +inPair.first.toPrettyString()
                    }
                    ReactHTML.td {
                        +inPair.second.counter.toString()
                    }
                    ReactHTML.td {
                        +outPair.first.toPrettyString()
                    }
                    ReactHTML.td {
                        +outPair.second.counter.toString()
                    }
                }
            }
        }
    }
}
