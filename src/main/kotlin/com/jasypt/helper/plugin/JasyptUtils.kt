package com.jasypt.helper.plugin

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig
import java.security.Security
import javax.crypto.Cipher

object JasyptUtils {

    private val defaultAlgorithms: List<String> = listOf(
            "PBEWithMD5AndDES",
            "PBEWithMD5AndTripleDES",
            "PBEWithSHA1AndDESede",
            "PBEWithSHA1AndRC2_40",
            "PBEWITHHMACSHA512ANDAES_256"
    )

    private fun getAvailablePBEAlgorithms(): List<String> {
        val algorithms = mutableSetOf<String>()

        try {
            val providers = Security.getProviders()

            for (provider in providers) {
                val services = provider.services
                for (service in services) {
                    if (service.type == "Cipher" && service.algorithm.startsWith("PBE", ignoreCase = true)) {
                        algorithms.add(service.algorithm)
                    }
                }
            }

            try {
                Cipher.getMaxAllowedKeyLength("AES")
            } catch (_: Exception) {
            }

        } catch (e: Exception) {
            println("Failed to detect PBE algorithms: ${e.message}")
        }

        /*if (algorithms.isNotEmpty()) {
            println("Detected ${algorithms.size} PBE algorithms: ${algorithms.sorted().joinToString(", ")}")
        } else {
            println("No PBE algorithms detected, using default list")
        }*/

        return if (algorithms.isNotEmpty()) {
            algorithms.sorted().toList()
        } else {
            defaultAlgorithms
        }
    }

    fun getAllAlgorithms(): List<String> {
        val available = getAvailablePBEAlgorithms().toMutableSet()
        available.addAll(defaultAlgorithms)
        return available.sorted()
    }

    private fun createEncryptor(password: String, algorithm: String): StandardPBEStringEncryptor {
        val encryptor = StandardPBEStringEncryptor()
        val config = EnvironmentStringPBEConfig()
        config.password = password
        config.algorithm = algorithm
        encryptor.setConfig(config)
        return encryptor
    }

    fun encryptContent(content: String, password: String, algorithm: String): String {
        val encryptor = createEncryptor(password, algorithm)

        val decPattern = """DEC\((.*)\)""".toRegex()
        return decPattern.replace(content) { matchResult ->
            val plainText = matchResult.groupValues[1]
            val encrypted = encryptor.encrypt(plainText)
            "ENC($encrypted)"
        }
    }

    fun decryptContent(content: String, password: String, algorithm: String): String {
        val encryptor = createEncryptor(password, algorithm)

        val encPattern = """ENC\((.*)\)""".toRegex()
        return encPattern.replace(content) { matchResult ->
            val encryptedText = matchResult.groupValues[1]
            val decrypted = encryptor.decrypt(encryptedText)
            "DEC($decrypted)"
        }
    }

    fun isAlgorithmAvailable(algorithm: String): Boolean {
        return try {
            Cipher.getInstance(algorithm)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getRecommendedAlgorithm(): String {
        val available = getAvailablePBEAlgorithms()

        val aesAlgorithms = available.filter { it.contains("AES", ignoreCase = true) }
        if (aesAlgorithms.isNotEmpty()) {
            return aesAlgorithms.minOf { it }
        }

        val sha256Algorithms = available.filter { it.contains("SHA256", ignoreCase = true) }
        if (sha256Algorithms.isNotEmpty()) {
            return sha256Algorithms.minOf { it }
        }

        val sha1Algorithms = available.filter { it.contains("SHA1", ignoreCase = true) }
        if (sha1Algorithms.isNotEmpty()) {
            return sha1Algorithms.minOf { it }
        }

        return if (available.isNotEmpty()) available.first() else defaultAlgorithms.first()
    }
}
