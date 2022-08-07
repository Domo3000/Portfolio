import kotlinx.browser.document
import react.create
import react.dom.client.hydrateRoot

fun main() {
    val state = Connect4State()

    document.addEventListener("Connect4", {
        state.reset()
        document.getElementById("external-holder")?.let {
            hydrateRoot(it, x(state).create())
        }
    })

    document.addEventListener("Connect4Cleanup", {
        state.reset()
    })
}
