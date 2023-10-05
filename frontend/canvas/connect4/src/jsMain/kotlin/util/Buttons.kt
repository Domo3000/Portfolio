package util

import emotion.react.css
import react.FC
import react.Props
import react.dom.events.MouseEventHandler
import react.dom.html.ReactHTML
import web.cssom.Color
import web.cssom.pct
import web.cssom.px
import web.html.HTMLButtonElement

data class Button(
    val text: String,
    val color: Color,
    val enabled: Boolean,
    val onClick: MouseEventHandler<HTMLButtonElement>
)

private external interface ButtonProps : Props {
    var text: String
    var color: Color
    var enabled: Boolean
    var onClick: MouseEventHandler<HTMLButtonElement>
    var width: Double?
}

private val button = FC<ButtonProps> { props ->
    ReactHTML.button {
        +props.text
        css {
            width = props.width?.pct ?: 100.0.pct
            padding = 15.px
            if (props.enabled) {
                background = props.color
            } else {
                color = props.color
            }
        }
        onClick = props.onClick
    }
}

external interface ButtonRowProps : Props {
    var buttons: List<Button>
}

val buttonRow = FC<ButtonRowProps> { props ->
    ReactHTML.div {
        props.buttons.forEach {
            button {
                text = it.text
                color = it.color
                enabled = it.enabled
                onClick = it.onClick
                width = (100.0 / props.buttons.size)
            }
        }
    }
}
