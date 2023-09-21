import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import routing.installRouting
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteTest {
    private fun routingTest(block: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            application {
                installRouting()
            }

            block(this)
        }
    }

    @Test
    fun healthCheck() {
        routingTest {
            val response = client.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Healthy!", response.body())
        }
    }

    @Test
    fun getRobots() {
        routingTest {
            val expected = """User-agent: *
                              |Allow: /""".trimMargin()

            val response = client.get("/robots.txt")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(expected, response.body())
        }
    }

    @Test
    fun getStyles() {
        routingTest {
            val response = client.get("/static/styles.css")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.CSS.withCharset(Charsets.UTF_8), response.contentType())
        }
    }

    @Test
    fun getAssets() {
        routingTest {
            val response = client.get("/static/favicon-16x16.png")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Image.PNG, response.contentType())
        }
    }

    @Test
    fun getIndex() {
        testApplication {
            application {
                installRouting()
            }

            val response = client.get("/anything-else")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Html.withCharset(Charsets.UTF_8), response.contentType())
        }
    }
}