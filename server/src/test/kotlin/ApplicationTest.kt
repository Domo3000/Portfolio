import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO get it to work
class ApplicationTest {
    @Test
    fun healthCheck() {
        testApplication {
            val response = client.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Healthy!", response.body())
        }
    }
}
