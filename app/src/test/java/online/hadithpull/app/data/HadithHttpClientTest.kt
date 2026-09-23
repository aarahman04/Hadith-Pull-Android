package online.hadithpull.app.data

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class HadithHttpClientTest {
    private val server = MockWebServer()
    private lateinit var client: HadithHttpClient

    @Before
    fun setUp() {
        server.start()
        client = HadithHttpClient(apiKey = "test-key", baseUrl = server.url("/").toString())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `hadiths envelope shape is parsed`() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"status":200,"hadiths":{"data":[{"hadithEnglish":"Text.","hadithNumber":1}]}}""")
                .build(),
        )
        val outcome = client.fetch("sahih-bukhari", 1) as HttpOutcome.Ok
        assertEquals("Text.", outcome.item?.hadithEnglish)
    }

    @Test
    fun `bare data envelope shape is parsed`() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body("""{"data":[{"hadithEnglish":"Text.","hadithNumber":1}]}""")
                .build(),
        )
        val outcome = client.fetch("sahih-bukhari", 1) as HttpOutcome.Ok
        assertEquals("Text.", outcome.item?.hadithEnglish)
    }

    @Test
    fun `empty data array is a miss with no item`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("""{"hadiths":{"data":[]}}""").build())
        val outcome = client.fetch("mishkat", 1) as HttpOutcome.Ok
        assertNull(outcome.item)
    }

    @Test
    fun `404 is NotOk`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(404).body("""{"status":404,"message":"Hadiths not found."}""").build())
        assertTrue(client.fetch("musnad-ahmad", 1) is HttpOutcome.NotOk)
    }

    @Test
    fun `429 is Busy`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(429).build())
        assertTrue(client.fetch("sahih-bukhari", 1) is HttpOutcome.Busy)
    }

    @Test
    fun `401 is Rejected`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(401).build())
        assertTrue(client.fetch("sahih-bukhari", 1) is HttpOutcome.Rejected)
    }

    @Test
    fun `403 is Rejected`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(403).build())
        assertTrue(client.fetch("sahih-bukhari", 1) is HttpOutcome.Rejected)
    }

    @Test
    fun `malformed JSON body throws SerializationException`() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("not json").build())
        try {
            client.fetch("sahih-bukhari", 1)
            throw AssertionError("expected SerializationException")
        } catch (e: SerializationException) {
            // expected
        }
    }
}
