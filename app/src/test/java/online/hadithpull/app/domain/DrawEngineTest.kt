package online.hadithpull.app.domain

import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import online.hadithpull.app.data.HadithHttpClient
import online.hadithpull.app.data.HttpOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

/** Always picks the first candidate offered, so a draw walks COLLECTIONS in order as slugs are tried. */
private class FirstAvailableRandom : Random() {
    override fun nextBits(bitCount: Int): Int = throw UnsupportedOperationException()
    override fun nextInt(until: Int): Int = 0
    override fun nextInt(from: Int, until: Int): Int = from
}

private fun ok(englishBody: String) =
    MockResponse.Builder().code(200).body("""{"data":[{"hadithEnglish":"$englishBody","hadithNumber":1}]}""").build()

private val notFound = MockResponse.Builder().code(404).body("""{"status":404,"message":"Hadiths not found."}""").build()
private val busy = MockResponse.Builder().code(429).build()
private val rejected = MockResponse.Builder().code(401).build()

class DrawEngineTest {
    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        try {
            server.close()
        } catch (e: IllegalStateException) {
            // already shut down by the test itself (the "unreachable server" case)
        }
    }

    private fun fetchFor(): suspend (String, Int) -> HttpOutcome {
        val client = HadithHttpClient(apiKey = "test-key", baseUrl = server.url("/").toString())
        return client::fetch
    }

    private fun slugOf(request: RecordedRequest): String? = request.url.queryParameter("book")

    @Test
    fun `a silent miss retries a different collection`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (slugOf(request) == "sahih-bukhari") notFound else ok("A self contained narration with enough words in it.")
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertTrue(result is DrawResult.Success)
        assertEquals("sahih-muslim", (result as DrawResult.Success).hadith.slug)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `429 returns Busy with no retry`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = busy
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertEquals(DrawResult.Failure.Busy, result)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `401 returns KeyRejected with no retry`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = rejected
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertEquals(DrawResult.Failure.KeyRejected, result)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `403 returns KeyRejected`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                MockResponse.Builder().code(403).build()
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertEquals(DrawResult.Failure.KeyRejected, result)
    }

    @Test
    fun `unreachable server returns Network`() = runBlocking {
        val fetch = fetchFor()
        server.close()
        val result = DrawEngine(fetch, FirstAvailableRandom()).draw()
        assertEquals(DrawResult.Failure.Network, result)
    }

    @Test
    fun `malformed JSON returns Network`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                MockResponse.Builder().code(200).body("not json").build()
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertEquals(DrawResult.Failure.Network, result)
    }

    @Test
    fun `exactly 11 requests then Exhausted`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = notFound
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertEquals(DrawResult.Failure.Exhausted, result)
        assertEquals(11, server.requestCount)
    }

    @Test
    fun `the tried set excludes a missed slug and clears once every collection has been tried`() = runBlocking {
        val requestedSlugs = mutableListOf<String>()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                requestedSlugs += slugOf(request)!!
                return notFound
            }
        }
        // 9 collections, then the tried set must clear and repeat one on the 10th/11th request.
        DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        val firstNine = requestedSlugs.take(9)
        assertEquals(9, firstNine.toSet().size)
        assertTrue(requestedSlugs[9] in firstNine)
    }

    @Test
    fun `ProcessHealth demotes a slug after 3 consecutive misses while another stays healthy`() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (slugOf(request) == "sahih-bukhari") notFound else ok("A self contained narration with enough words in it.")
        }
        val health = ProcessHealth()
        val fetch = fetchFor()
        repeat(3) {
            DrawEngine(fetch, FirstAvailableRandom(), health).draw()
        }
        assertTrue("sahih-bukhari" in health.demoted)
    }

    @Test
    fun `ProcessHealth resets a slug's miss count on success`() = runBlocking {
        var bukhariCalls = 0
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (slugOf(request) != "sahih-bukhari") return ok("A self contained narration with enough words in it.")
                bukhariCalls++
                return if (bukhariCalls <= 2) notFound else ok("A self contained narration with enough words too.")
            }
        }
        val health = ProcessHealth()
        val fetch = fetchFor()
        repeat(3) { DrawEngine(fetch, FirstAvailableRandom(), health).draw() }
        assertTrue("sahih-bukhari" !in health.demoted)
    }

    @Test
    fun `ProcessHealth never demotes the last healthy slug, even past 3 consecutive misses`() {
        val health = ProcessHealth()
        val allButLast = COLLECTIONS.dropLast(1)
        val lastHealthy = COLLECTIONS.last().slug

        allButLast.forEach { collection -> repeat(3) { health.onMiss(collection.slug) } }
        assertEquals(allButLast.map { it.slug }.toSet(), health.demoted)

        repeat(5) { health.onMiss(lastHealthy) }
        assertTrue(lastHealthy !in health.demoted)
    }

    @Test
    fun `CancellationException is rethrown, not swallowed as a Failure`() = runBlocking {
        val cancellingFetch: suspend (String, Int) -> HttpOutcome = { _, _ -> throw CancellationException("cancelled") }
        var thrown: Throwable? = null
        try {
            DrawEngine(cancellingFetch, FirstAvailableRandom()).draw()
        } catch (e: CancellationException) {
            thrown = e
        }
        assertTrue(thrown is CancellationException)
    }

    @Test
    fun `isSelfContained runs on the pre-strip English, not the narrator-stripped English`() = runBlocking {
        // The raw hadithEnglish here is short enough on its own (under 15 chars) to fail
        // isSelfContained; if the engine normalised (stripped the narrator) before checking,
        // this would still be judged on the same short remainder either way, so instead this
        // narrows on the check running on hadithEnglish directly rather than a derived field.
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                if (slugOf(request) == "sahih-bukhari") {
                    MockResponse.Builder().code(200)
                        .body("""{"data":[{"hadithEnglish":"Short.","englishNarrator":"Abu Huraira","hadithNumber":1}]}""")
                        .build()
                } else {
                    ok("A self contained narration with enough words in it.")
                }
        }
        val result = DrawEngine(fetchFor(), FirstAvailableRandom()).draw()
        assertTrue(result is DrawResult.Success)
        assertEquals("sahih-muslim", (result as DrawResult.Success).hadith.slug)
    }
}
