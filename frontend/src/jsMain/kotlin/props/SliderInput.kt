package props

import web.cssom.NamedColor
import web.cssom.None
import web.cssom.pct
import emotion.react.css
import web.html.HTMLInputElement
import react.FC
import react.Props
import react.dom.events.ChangeEventHandler
import react.dom.html.ReactHTML
import web.html.InputType

external interface SliderInputProps : Props {
    var id: String?
    var value: String
    var min: Double
    var max: Double
    var step: Double
    var onChange: ChangeEventHandler<HTMLInputElement>
}

val sliderInput = FC<SliderInputProps> { props ->
    ReactHTML.input {
        props.id?.let {
            id = it
        }
        type = InputType.range
        min = props.min
        max = props.max
        step = props.step
        value = props.value
        css {
            appearance = None.none
            width = 100.pct
            outline = None.none
            backgroundColor = NamedColor.darkgray
        }
        onChange = props.onChange
    }
}