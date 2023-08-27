package props

import web.cssom.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import kotlin.text.Typography.nbsp

class CodeLine(val elements: List<Pair<String, Color>> = listOf("\t".white()))

external interface FormattedCodeProps : Props {
    var lines: List<CodeLine>
}

val formattedCode = FC<FormattedCodeProps> { props ->
    ReactHTML.div {
        css {
            padding = 10.px
            backgroundColor = rgb(20, 20, 20)
            borderColor = NamedColor.gray
            borderStyle = LineStyle.inset
            borderWidth = LineWidth.thick
            display = Display.inlineBlock
            fontFamily = string("Monaco, monospace") // TODO different fonts
        }
        props.lines.forEach { line ->
            ReactHTML.p {
                css {
                    backgroundColor = rgb(20, 20, 20)
                    margin = 0.px
                }
                line.elements.forEach { (text, colour) ->
                    ReactHTML.span {
                        css {
                            backgroundColor = rgb(20, 20, 20)
                            color = colour
                        }
                        +text.replace("\t", "$nbsp   ")
                    }
                }
            }
        }
    }
}

fun String.colored(color: Color) = this to color
fun String.white() = colored(NamedColor.white)
fun String.yellow() = colored(NamedColor.yellow)
fun String.orange() = colored(NamedColor.orange)
fun String.purple() = colored(NamedColor.mediumpurple)
fun String.green() = colored(NamedColor.green)
fun String.blue() = colored(NamedColor.cornflowerblue)