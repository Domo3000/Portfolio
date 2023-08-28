import kotlinx.browser.window
import web.http.Request

object Requests {
    fun getMessage(url: String, callback: (Message?) -> Unit) {
        window.fetch(Request(url)).then { response ->
            response.text().then { text ->
                callback(Messages.decode(text))
            }
        }
    }
}
