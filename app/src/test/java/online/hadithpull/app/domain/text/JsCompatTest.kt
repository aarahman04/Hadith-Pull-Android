package online.hadithpull.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class JsCompatTest {
    @Test
    fun `jsTrim strips ASCII whitespace from both ends`() {
        assertEquals("hello", "  hello  ".jsTrim())
    }

    @Test
    fun `jsTrim strips U+00A0 non-breaking space`() {
        assertEquals("hello", " hello ".jsTrim())
    }

    @Test
    fun `jsTrim strips U+FEFF byte order mark`() {
        assertEquals("hello", "﻿hello﻿".jsTrim())
    }

    @Test
    fun `jsTrim leaves internal whitespace untouched`() {
        assertEquals("hello world", " hello world ".jsTrim())
    }
}
