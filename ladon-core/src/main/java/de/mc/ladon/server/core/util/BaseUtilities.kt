package de.mc.ladon.server.core.util

import de.mc.ladon.server.core.exceptions.LadonIllegalArgumentException
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


/**
 * @author Ralf Ulrich
 * 28.08.16
 */


fun Long.humanReadable(): String {
    return ByteFormat.humanReadableByteCount(this, true)
}

fun getLadonHome() = System.getProperty("ladon.home") ?: "${System.getProperty("user.home")}${File.separator}ladon_data"
fun getSystemDir() = getLadonHome() + File.separator + "system"

fun <T> List<T>.second(): T {
    return this[1]
}


fun String?.toUUID(): UUID {
    if (this == null) throw LadonIllegalArgumentException("Wrong UUID format")
    return UUID.fromString(this)
}

fun Any.getLogger() = LoggerFactory.getLogger(this::class.java)



