package about

import Requests
import canvas.ExternalCanvas
import connect4.game.TrainingGroups
import connect4.messages.Connect4Messages
import connect4.messages.TrainingResultMessage
import connect4.messages.TrainingResultsMessage
import react.*
import util.Button
import util.ColoredTrainingGroups
import util.buttonRow
import web.cssom.NamedColor

const val margin = 3
const val pow = 3.0
const val scale = 1.16

enum class Label {
    InputOutput,
    BatchNorm,
    ValidLayer,
    SameLayer,
    LongTraining
}

private fun List<Any>.optionalLabel(label: Label): Label? = if (this.isEmpty()) null else label

class Connect4About : ExternalCanvas() {
    override val name: String = "Connect4About"

    override val component: FC<Props>
        get() = FC {
            val (currentMode, setCurrentMode) = useState<Pair<Label, TrainingView>?>(null)

            val (inputTrainingHistory, setInputTrainingHistory) = useState<List<TrainingResultMessage>>(emptyList())
            val (batchNormTrainingHistory, setBatchNormTrainingHistory) = useState<List<TrainingResultMessage>>(emptyList())

            val (validLayerTrainingHistory, setValidLayerTrainingHistory) = useState<List<TrainingResultMessage>>(
                emptyList()
            )

            val (sameLayerTrainingHistory, setSameLayerTrainingHistory) = useState<List<TrainingResultMessage>>(
                emptyList()
            )

            val (longTrainingHistory, setLongTrainingHistory) = useState<List<TrainingResultMessage>>(emptyList())

            val labels = listOfNotNull(
                inputTrainingHistory.optionalLabel(Label.InputOutput),
                batchNormTrainingHistory.optionalLabel(Label.BatchNorm),
                validLayerTrainingHistory.optionalLabel(Label.ValidLayer),
                sameLayerTrainingHistory.optionalLabel(Label.SameLayer),
                longTrainingHistory.optionalLabel(Label.LongTraining),
            )

            TrainingView.entries.forEach { view ->
                buttonRow {
                    buttons = labels.map { type ->
                        Button(
                            "$type $view",
                            NamedColor.aqua,
                            currentMode != type to view
                        ) {
                            setCurrentMode(type to view)
                        }
                    }
                }
            }

            currentMode?.let {
                when (currentMode.first) {
                    Label.InputOutput -> TrainingHistory {
                        results = inputTrainingHistory
                        group = ColoredTrainingGroups.inputOutputExperiment
                        view = currentMode.second
                    }

                    Label.BatchNorm -> TrainingHistory {
                        results = batchNormTrainingHistory
                        group = ColoredTrainingGroups.batchNormExperiment
                        view = currentMode.second
                    }

                    Label.ValidLayer -> TrainingHistory {
                        results = validLayerTrainingHistory
                        group = ColoredTrainingGroups.mixedLayerExperiment(TrainingGroups.validLayerExperiment)
                        view = currentMode.second
                    }

                    Label.SameLayer -> TrainingHistory {
                        results = sameLayerTrainingHistory
                        group = ColoredTrainingGroups.mixedLayerExperiment(TrainingGroups.sameLayerExperiment)
                        view = currentMode.second
                    }

                    Label.LongTraining -> TrainingHistory {
                        results = longTrainingHistory
                        group = ColoredTrainingGroups.longTrainingExperiment
                        view = currentMode.second
                    }
                }
            }

            /*
            // TODO (Long)CombinationTrainingExplanation and different SmallTrainingExplanation
            Explanation {
                group = ColoredTrainingGroups.paddingExperiment
            }

             */

            // TODO default to MixedLayer later
            useEffect(validLayerTrainingHistory) {
                if (validLayerTrainingHistory.isNotEmpty()) {
                    setCurrentMode(Label.ValidLayer to TrainingView.History)
                }
            }

            useEffectOnce {
                listOf(
                    Label.InputOutput to setInputTrainingHistory,
                    Label.BatchNorm to setBatchNormTrainingHistory,
                    Label.ValidLayer to setValidLayerTrainingHistory,
                    Label.SameLayer to setSameLayerTrainingHistory,
                    Label.LongTraining to setLongTrainingHistory
                ).forEach { (label, stateSetter) ->
                    val name = label.toString().replaceFirstChar { it.lowercase() }

                    Requests.get("/static/$name-experiment.json") { response ->
                        (Connect4Messages.decode(response) as? TrainingResultsMessage)?.let {
                            stateSetter(it.results)
                        }
                    }
                }
            }
        }

    override fun cleanUp() {}

    override fun initialize() {}

    init {
        initEventListeners()
    }
}