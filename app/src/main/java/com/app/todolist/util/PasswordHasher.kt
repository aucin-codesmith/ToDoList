package com.app.todolist.util

import java.security.MessageDigest

/**
 * Utility sederhana untuk hashing password sebelum disimpan ke SQLite.
 * Password TIDAK PERNAH disimpan dalam bentuk plain text.
 *
 * Catatan: SHA-256 murni (tanpa salt) dipakai di sini demi kesederhanaan
 * untuk app skala kecil. Untuk produksi yang lebih serius, pertimbangkan
 * menambahkan salt per-user atau memakai library seperti BCrypt.
 */
object PasswordHasher {

    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, hashed: String): Boolean {
        return hash(password) == hashed
    }
}