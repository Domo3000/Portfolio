import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// TODO move to a common package if needed elsewhere
object Requests {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                classDiscriminator = "class"
            })
        }
    }

    fun getMessage(url: String, callback: (Message?) -> Unit) {
        MainScope().launch {
            val response = client.get(url)
            var message: Message? = null

            if (response.status.isSuccess()) {
                message = Messages.decode(response.bodyAsText())
            }

            callback(message)
        }
    }
}
