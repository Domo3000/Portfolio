package neural

import java.io.File

class StoredHandler {
    companion object {
        fun loadStored(path: String, names: List<String> = emptyList(), prefix: String? = null, silent: Boolean = false): List<StoredNeuralAI> {
            val directory = File("${System.getProperty("user.dir")}/$path")

            println(directory)

            if(!silent) {
                println("Loading from storage:")
            }

            val loadedNeurals = mutableListOf<StoredNeuralAI>()

            if (directory.isDirectory) {
                directory.walk().forEach { neuralDirectory ->
                    if (neuralDirectory != directory && neuralDirectory.isDirectory) {
                        val name = neuralDirectory.name
                        if ((names.isEmpty() && prefix == null) ||
                            (prefix == null && names.contains(name)) ||
                            (prefix != null && name.startsWith(prefix))
                        ) {
                            try {
                                val loaded = StoredNeuralAI.fromStorage(path, name)
                                if(!silent) {
                                    println(loaded.info())
                                }
                                loadedNeurals += loaded
                            } catch (e: Exception) {
                                println(e)
                            }
                        }
                    }
                }
            }
            return loadedNeurals
        }
    }

    private val neurals = mutableListOf<StoredNeuralAI>()

    fun allNeurals() = neurals.toList()

    fun loadStoredNeurals(path: String = "neurals", names: List<String> = emptyList(), prefix: String? = null, silent: Boolean = false) {
        neurals.addAll(loadStored(path, names, prefix, silent))
    }
}
