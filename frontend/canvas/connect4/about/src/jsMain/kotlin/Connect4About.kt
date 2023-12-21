package about

import Requests
import about.explanation.Explanation
import canvas.ExternalCanvas
import connect4.game.TrainingGroups
import connect4.messages.Connect4Messages
import connect4.messages.TrainingResultMessage
import connect4.messages.TrainingResultsMessage
import react.*
import util.Button
import util.TrainingGroupColors
import util.buttonRow
import web.cssom.NamedColor

const val margin = 3
const val pow = 3.0
const val scale = 1.16

enum class Label {
    Input,
    Output,
    BatchNorm,
    MixedLayer,
    LongTraining
}

private fun List<Any>.optionalLabel(label: Label): Label? = if (this.isEmpty()) null else label

class Connect4About : ExternalCanvas() {
    override val name: String = "Connect4About"

    override val component: FC<Props>
        get() = FC {
            val (currentMode, setCurrentMode) = useState<Pair<Label, TrainingView>?>(null)

            val (inputTrainingHistory, setInputTrainingHistory) = useState<List<TrainingResultMessage>>(emptyList())
            val (outputTrainingHistory, setOutputTrainingHistory) = useState<List<TrainingResultMessage>>(emptyList())
            val (batchNormTrainingHistory, setBatchNormTrainingHistory) = useState<List<TrainingResultMessage>>(
                emptyList()
            )

            val (mixedLayerTrainingHistory, setMixedLayerTrainingHistory) = useState<List<TrainingResultMessage>>(
                emptyList()
            )

            val (longTrainingHistory, setLongTrainingHistory) = useState<List<TrainingResultMessage>>(emptyList())

            val labels = listOfNotNull(
                inputTrainingHistory.optionalLabel(Label.Input),
                outputTrainingHistory.optionalLabel(Label.Output),
                batchNormTrainingHistory.optionalLabel(Label.BatchNorm),
                mixedLayerTrainingHistory.optionalLabel(Label.MixedLayer),
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
                val (trainingHistory, trainingGroup, trainingColor) = when (currentMode.first) {
                    Label.Input -> Triple(
                        inputTrainingHistory,
                        TrainingGroups.inputExperiment,
                        TrainingGroupColors.inputExperiment
                    )

                    Label.Output -> Triple(
                        outputTrainingHistory,
                        TrainingGroups.outputExperiment,
                        TrainingGroupColors.outputExperiment
                    )

                    Label.BatchNorm -> Triple(
                        batchNormTrainingHistory,
                        TrainingGroups.batchNormExperiment,
                        TrainingGroupColors.batchNormExperiment
                    )

                    Label.MixedLayer -> Triple(
                        mixedLayerTrainingHistory,
                        TrainingGroups.mixedLayerExperiment,
                        TrainingGroupColors.mixedLayerExperiment
                    )

                    Label.LongTraining -> Triple(
                        longTrainingHistory,
                        TrainingGroups.longTrainingExperiment,
                        TrainingGroupColors.longTrainingExperiment
                    )
                }

                TrainingHistory {
                        results = trainingHistory
                        group = trainingGroup
                        color = trainingColor
                        view = currentMode.second
                }

                Explanation {
                    group = trainingGroup
                }
            }

            useEffect(mixedLayerTrainingHistory) {
                if (mixedLayerTrainingHistory.isNotEmpty()) {
                    setCurrentMode(Label.MixedLayer to TrainingView.History)
                }
            }

            useEffectOnce {
                listOf(
                    Label.Input to setInputTrainingHistory,
                    Label.Output to setOutputTrainingHistory,
                    Label.BatchNorm to setBatchNormTrainingHistory,
                    Label.MixedLayer to setMixedLayerTrainingHistory,
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