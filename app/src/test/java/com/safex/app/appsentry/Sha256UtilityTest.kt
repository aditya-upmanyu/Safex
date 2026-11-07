// test/appsentry/Sha256UtilityTest.kt
package com.safex.app.appsentry

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.security.MessageDigest

class Sha256UtilityTest {

    private fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val stream = ByteArrayInputStream(bytes)
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        val hashBytes = digest.digest()
        val sb = StringBuilder()
        for (b in hashBytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    @Test
    fun testSha256EmptyInput() {
        val hash = calculateSha256(ByteArray(0))
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash)
    }

    @Test
    fun testSha256HelloWorld() {
        val hash = calculateSha256("hello world".toByteArray(Charsets.UTF_8))
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash)
    }

    @Test
    fun testSha256SafeXSample() {
        val sample = "SAFEx Security Engine 2026".toByteArray(Charsets.UTF_8)
        val hash = calculateSha256(sample)
        assertEquals(64, hash.length)
    }
}
