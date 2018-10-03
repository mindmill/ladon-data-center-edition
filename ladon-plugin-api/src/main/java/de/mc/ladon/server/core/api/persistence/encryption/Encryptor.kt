package de.mc.ladon.server.core.api.persistence.encryption

/**
 * @author Ralf Ulrich
 * 21.10.16
 */
interface Encryptor {

    fun encrypt(value: String): String

    fun encrypt(value: ByteArray): ByteArray

    fun decrypt(value: String): String

    fun decrypt(value: ByteArray): ByteArray
}