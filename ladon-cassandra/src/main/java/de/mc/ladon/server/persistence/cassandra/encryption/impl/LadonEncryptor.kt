/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.encryption.impl

import com.google.common.io.BaseEncoding
import de.mc.ladon.server.persistence.cassandra.encryption.api.Encryptor
import org.apache.cassandra.utils.MD5Digest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Simple encrypt decrypt util
 * @author Ralf Ulrich
 * on 21.10.16.
 */
class LadonEncryptor(val key: String = "NoPW") : Encryptor {

    private val cipher = "AES/CBC/PKCS5PADDING"
    private val alg = "AES"
    private val keysize = 16

    override fun encrypt(value: String): String {
        return encodeBase64String(encrypt(value.toByteArray()))
    }


    override fun encrypt(value: ByteArray): ByteArray {
        val ivbytes = generateRandom128bits()
        val keybytes = get128BitKey(key)
        val iv = IvParameterSpec(ivbytes)
        val skeySpec = SecretKeySpec(keybytes, alg)
        val cipher = Cipher.getInstance(cipher)
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
        val encrypted = cipher.doFinal(value)
        return ivbytes + encrypted
    }

    override fun decrypt(value: ByteArray): ByteArray {
        val ivbytes = value.copyOfRange(0, keysize)
        val keybytes = get128BitKey(key)
        val iv = IvParameterSpec(ivbytes)
        val skeySpec = SecretKeySpec(keybytes, alg)
        val cipher = Cipher.getInstance(cipher)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
        return cipher.doFinal(value.copyOfRange(keysize, value.size))
    }

    override fun decrypt(value: String): String {
        return String(decrypt(decodeBase64(value)))
    }

    fun generateRandom128bits(): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(keysize)
        random.nextBytes(bytes)
        return bytes
    }


    fun get128BitKey(key: String): ByteArray {
        return MD5Digest.compute(key).bytes
    }


    private fun encodeBase64String(input: ByteArray): String {
        return BaseEncoding.base64().encode(input)
    }

    private fun decodeBase64(input: String): ByteArray {
        return BaseEncoding.base64().decode(input)
    }


}