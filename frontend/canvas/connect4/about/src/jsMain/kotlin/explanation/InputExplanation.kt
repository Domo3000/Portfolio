package about.explanation

import about.util.LimitedDescription
import connect4.game.InputType
import connect4.game.Player
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML
import util.Button
import util.TrainingGroupColors
import util.buttonRow
import util.rgb
import web.cssom.*

private val field: Array<Array<Player?>> = arrayOf(
    Array(7) { null },
    Array(7) { null },
    Array(7) { if (it == 2) Player.FirstPlayer else null },
    Array(7) { if (it % 2 != 0) Player.FirstPlayer else Player.SecondPlayer },
    Array(7) { if (it % 2 == 0) Player.FirstPlayer else Player.SecondPlayer },
    Array(7) { if (it % 2 != 0) Player.FirstPlayer else Player.SecondPlayer }
)

private fun Player?.switch(switch: Boolean) = if(switch) this?.switch() else this

private fun Player?.toNumber(opponent: Int) = when (this) {
    Player.FirstPlayer -> 1
    Player.SecondPlayer -> opponent
    null -> 0
}

private external interface FieldExplanationProps : Props {
    var opponent: Int
    var switch: Boolean
}

private val FieldExplanation = FC<FieldExplanationProps> { props ->
    ReactHTML.div {
        css {
            width = 33.pct
            borderStyle = LineStyle.solid
            borderWidth = LineWidth.thin
            display = Display.grid
            gridTemplateColumns = repeat(7, 1.fr)
            justifyItems = JustifyItems.center
        }
        field.forEach { row ->
                row.map {
                    ReactHTML.div {
                        +"${it.switch(props.switch).toNumber(props.opponent)}"
                    }
            }
        }
    }
}

external interface InputExplanationProps : Props {
    var shownInput: InputType
    var setShownInput: (InputType) -> Unit
}

val InputExplanation = FC<InputExplanationProps> { props ->
    buttonRow {
        buttons = InputType.entries.map { input ->
            Button(
                "$input Input",
                TrainingGroupColors.inputExperiment(LimitedDescription(inputType = input)).rgb(),
                props.shownInput == input
            ) {
                if (props.shownInput != input) {
                    props.setShownInput(input)
                }
            }
        }
    }

    ReactHTML.div {
        ReactHTML.strong {
            css {
                width = 100.px
                margin = Auto.auto
            }
            +props.shownInput.name
        }
    }

    ReactHTML.div {
        css {
            height = 100.px
            display = Display.flex
            alignItems = AlignItems.center
            justifyContent = JustifyContent.center
        }
        val (singular, opponent) = when(props.shownInput) {
            InputType.SingularMinus -> true to -1
            InputType.SingularPlus -> true to 2
            InputType.DualNeutral -> false to 0
            InputType.DualMinus -> false to -1
            InputType.DualPlus -> false to 2
        }

        if(singular) {
            FieldExplanation {
                this.opponent = opponent
                switch = false
            }
        } else {
            listOf(false, true).map {
                FieldExplanation {
                    this.opponent = opponent
                    switch = it
                }
            }
        }
    }
}