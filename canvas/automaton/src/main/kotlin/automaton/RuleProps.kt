package automaton

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.StateSetter
import react.dom.html.InputType
import react.dom.html.ReactHTML

external interface BooleanProps : Props {
    var active: Boolean?
}

private val booleanElement = FC<BooleanProps> { props ->
    val color = when (props.active) {
        true -> "Black"
        false -> "White"
        else -> "Lightgray"
    }

    ReactHTML.div {
        css {
            width = (100.0 / 3.0).pct
            height = 30.px
            backgroundColor = Color(color)
            float = Float.left
        }
    }
}

external interface RulePickerProps : Props {
    var rule: Int
    var active: Int
    var ruleSetter: StateSetter<Int>
}

private val rulePicker = FC<RulePickerProps> { props ->
    ReactHTML.div {
        css {
            width = 90.px
            float = Float.left
            margin = 2.px
        }
        ReactHTML.div {
            booleanElement {
                active = props.rule >= 4
            }
            booleanElement {
                active = (props.rule % 4) >= 2
            }
            booleanElement {
                active = (props.rule % 2) >= 1
            }
        }
        ReactHTML.div {
            booleanElement {
                active = null
            }
            ReactHTML.button {
                css {
                    width = (100.0 / 3.0).pct
                    height = 30.px
                    backgroundColor = Color(if (props.active.contains(props.rule)) "Black" else "White")
                    float = Float.left
                }
                onClick = {
                    props.ruleSetter(props.active.switch(props.rule))
                }
            }
            booleanElement {
                active = null
            }
        }
    }
}

external interface RowProps : Props {
    var active: Int
    var ruleSetter: StateSetter<Int>
    var wrapping: Boolean?
    var wrappingSetter: StateSetter<Boolean?>
}

val ruleRow = FC<RowProps> { props ->
    (7 downTo 0).map { i ->
        rulePicker {
            rule = i
            active = props.active
            ruleSetter = props.ruleSetter
        }
        if (i == 4) {
            ReactHTML.div {
                css {
                    width = 90.px
                    height = 60.px
                    float = Float.left
                    margin = 2.px
                    textAlign = TextAlign.center
                    backgroundColor = Color("White")
                }

                ReactHTML.div {
                    ReactHTML.input {
                        css {
                            maxWidth = 75.px
                            height = 25.px
                            backgroundColor = Color("White")
                        }
                        type = InputType.number
                        min = 1.0
                        max = 255.0
                        value = props.active.toString()
                        onChange = {
                            props.ruleSetter(it.target.value.toInt())
                        }
                    }
                }

                val (background, text) = when (props.wrapping) {
                    true -> "Black" to "White"
                    false -> "White" to "Black"
                    else -> "Lightgray" to "Black"
                }

                ReactHTML.button {
                    css {
                        width = 100.pct
                        height = 30.px
                        backgroundColor = Color(background)
                        color = Color(text)
                        float = Float.left
                    }
                    onClick = {
                        when {
                            props.wrapping == null -> props.wrappingSetter(true)
                            props.wrapping!! -> props.wrappingSetter(false)
                            else -> props.wrappingSetter(null)
                        }
                    }
                    +"Wrapping"
                }
            }
        }
    }
}
