import kotlinx.browser.window
import web.http.Request

object Requests {
    fun get(url: String, callback: (String) -> Unit) {
        window.fetch(Request(url)).then { response ->
            response.text().then { text ->
                callback((text))
            }
        }
    }

    fun getMessage(url: String, callback: (Message?) -> Unit) {
        get(url) { callback(Messages.decode(it)) }
    }
}
