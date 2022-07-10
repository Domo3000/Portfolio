import kotlinx.browser.document
import org.w3c.dom.events.Event
import react.create
import react.dom.client.hydrateRoot

fun main() {
    document.dispatchEvent(Event("Connect4Init")) // TODO in browser disable menu until init

    document.addEventListener("Connect4", {
        document.getElementById("external-holder")?.let {
            hydrateRoot(it, Connect4.create())
        }
    })
}
