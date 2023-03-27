package connect4.ai.neural

import java.io.File

object StoredHandler {
    private val neurals = mutableListOf<StoredNeuralAI>()

    fun allNeurals() = neurals.toList()

    fun loadStored(names: List<String> = emptyList()) {
        val before = neurals.toList()
        val directory = File("${System.getProperty("user.dir")}/neurals")

        if (directory.isDirectory) {
            directory.walk().forEach { neuralDirectory ->
                if (neuralDirectory != directory && neuralDirectory.isDirectory) {
                    val name = neuralDirectory.name
                    if (names.isEmpty() || names.contains(name)) {
                        try {
                            val loaded = StoredNeuralAI.fromStorage(name)
                            println("${neuralDirectory.path}: ${loaded.name}")
                            neurals += loaded
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                }
            }
        }

        val after = neurals.toList()

        println("Loaded from Storage:")
        after.filterNot { before.contains(it) }.forEach {
            println(it.info())
        }
    }
}