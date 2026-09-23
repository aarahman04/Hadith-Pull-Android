package online.hadithpull.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookmarkRepositoryTest {
    @Test
    fun `a blank name is invalid`() {
        assertNull(validFolderName("   "))
        assertNull(validFolderName(""))
    }

    @Test
    fun `a name over 40 characters is invalid`() {
        assertNull(validFolderName("a".repeat(41)))
        assertEquals("a".repeat(40), validFolderName("a".repeat(40)))
    }

    @Test
    fun `jsTrim is applied before validation`() {
        assertEquals("Ramadan", validFolderName("  Ramadan  "))
        assertEquals("Ramadan", validFolderName(" Ramadan "))
    }

    @Test
    fun `sortFolderSummaries orders case-insensitively by locale`() {
        val folders = listOf(
            FolderSummary(id = 1, name = "zebra", count = 0, createdAt = 0),
            FolderSummary(id = 2, name = "Apple", count = 0, createdAt = 0),
            FolderSummary(id = 3, name = "banana", count = 0, createdAt = 0),
        )
        val sorted = sortFolderSummaries(folders)
        assertEquals(listOf("Apple", "banana", "zebra"), sorted.map { it.name })
    }
}
