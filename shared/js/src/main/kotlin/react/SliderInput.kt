package react

import csstype.NamedColor
import csstype.None
import csstype.pct
import emotion.react.css
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.dom.events.ChangeEventHandler
import react.dom.html.InputType
import react.dom.html.ReactHTML

external interface SliderInputProps : Props {
    var value: String
    var min: Double
    var max: Double
    var step: Double
    var onChange: ChangeEventHandler<HTMLInputElement>
}

val sliderInput = FC<SliderInputProps> { props ->
    ReactHTML.input {
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