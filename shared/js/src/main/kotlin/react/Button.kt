package react

import csstype.*
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import react.*
import react.dom.events.MouseEventHandler
import react.dom.html.InputType
import react.dom.html.ReactHTML

external interface ButtonProps : Props {
    var text: String
    var disabled: Boolean?
    var onClick: MouseEventHandler<HTMLButtonElement>
    var width: Double?
}

typealias Button = Triple<String, Boolean, MouseEventHandler<HTMLButtonElement>>

external interface ButtonRowProps : Props {
    var buttons: List<Triple<String, Boolean, MouseEventHandler<HTMLButtonElement>>>
}

val buttonRow = FC<ButtonRowProps> { props ->
    ReactHTML.div {
        props.buttons.forEach {
            button {
                text = it.first
                disabled = it.second
                onClick = it.third
                width = (99.99 / props.buttons.size)
            }
        }
    }
}

val button = FC<ButtonProps> { props ->
    ReactHTML.button {
        +props.text
        css {
            width = props.width?.pct ?: 100.0.pct
            padding = 15.px
            //float = Float.left
            if (props.disabled == true) {
                textDecoration = TextDecoration.lineThrough
            }
        }
        onClick = props.onClick
        props.disabled?.let {
            disabled = it
        }
    }
}