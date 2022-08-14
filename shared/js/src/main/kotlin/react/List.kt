package react

import react.dom.html.ReactHTML

external interface ListProps : Props {
    var texts: List<String>
}

val list = FC<ListProps> { props ->
    ReactHTML.ul {
        props.texts.forEach { text ->
            ReactHTML.li {
                +text
            }
        }
    }
}