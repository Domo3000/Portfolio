import kotlinx.browser.document
import overview.Routing
import react.create
import react.dom.client.createRoot

private fun consoleBanner() {
    console.log("""#####   ####  #    #  ####
                  |#    # #    # ##  ## #    #
                  |#    # #    # # ## # #    #
                  |#    # #    # #    # #    #
                  |#    # #    # #    # #    #
                  |#####   ####  #    #  ####""".trimMargin())
}

fun main() {
    document.getElementById("script-holder")?.let {
        consoleBanner()
        createRoot(it).render(Routing.create())
    }
}
