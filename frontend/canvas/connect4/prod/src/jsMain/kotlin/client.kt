import kotlinx.browser.document

fun main() {
    val secure = document.location?.protocol == "https:"
    val port = if (secure) { 443 } else { 80 }
    Connect4("domo.software", port, secure)
}
