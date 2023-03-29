package connect4.ai.neural

import java.io.File

class StoredHandler {
    private val neurals = mutableListOf<StoredNeuralAI>()

    fun allNeurals() = neurals.toList()

    fun loadStored(names: List<String> = emptyList(), prefix: String? = null, silent: Boolean = false) {
        val directory = File("${System.getProperty("user.dir")}/neurals")

        if(!silent) {
            println("Loading from storage:")
        }

        if (directory.isDirectory) {
            directory.walk().forEach { neuralDirectory ->
                if (neuralDirectory != directory && neuralDirectory.isDirectory) {
                    val name = neuralDirectory.name
                    if ((names.isEmpty() && prefix == null) ||
                        (prefix == null && names.contains(name)) ||
                        (prefix != null && name.contains(prefix))
                    ) {
                        try {
                            val loaded = StoredNeuralAI.fromStorage(name)
                            if(!silent) {
                                println(loaded.info())
                            }
                            neurals += loaded
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                }
            }
        }
    }
}