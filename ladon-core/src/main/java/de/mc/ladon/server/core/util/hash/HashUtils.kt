/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.util.hash

import java.security.MessageDigest

/**
 * HashUtils
 * Created by Ralf Ulrich on 31.01.15.
 */
fun ByteArray.getSHA1Hash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    return md.digest(this)
}

fun ByteArray.getSHA256Hash(): ByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(this)
}

fun ByteArray.getMD5Hash(): ByteArray {
    val md = MessageDigest.getInstance("MD5")
    return md.digest(this)
}