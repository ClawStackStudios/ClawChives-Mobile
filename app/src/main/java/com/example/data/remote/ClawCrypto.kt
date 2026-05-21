package com.example.data.remote

import java.security.MessageDigest

object ClawCrypto {
    /**
     * Hashes a plaintext hu- key using SHA-256 and returns a lowercase 64-char hex string.
     */
    fun hashHumanKey(rawKey: String): String {
        require(rawKey.startsWith("hu-") || rawKey.startsWith("lb-")) { "Key must begin with 'hu-' or 'lb-' prefix" }
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawKey.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
