/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.util

import de.mc.ladon.server.core.persistence.DatabaseConstants
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger

/**
 * Chunked Streams
 * Created by ralfulrich on 02.02.15.
 */
class ChunkInputStream(val loader: ChunkLoader) : InputStream() {

    var chunkPosition = 0
    var currentChunk: ByteArray = loader.nextChunk() ?: ByteArray(0)


    override fun read(): Int {
        if (chunkPosition == currentChunk.size) {
            currentChunk = loader.nextChunk() ?: return -1
            chunkPosition = 0
        }
        return currentChunk.get(chunkPosition++).toInt().and(0xff)
    }

    override fun toString(): String {
        return "ChunkInputStream@${hashCode()} , chunkposition : $chunkPosition}"
    }
}

class ChunkOutputStream(val writer: ChunkWriter) : OutputStream() {

    var chunkPosition = 0
    var currentChunk = writer.getEmptyChunk() ?: ByteArray(0)

    override fun write(b: Int) {
        if (chunkPosition == currentChunk.size) swapChunk()
        if (currentChunk.size > 0) currentChunk[chunkPosition++] = b.toByte()
    }

    override fun close() {
        writer.writeChunk(currentChunk.copyOfRange(0, chunkPosition))
    }

    fun swapChunk() {
        writer.writeChunk(currentChunk)
        currentChunk = writer.getEmptyChunk() ?: ByteArray(0)
        chunkPosition = 0
    }
}


interface ChunkLoader {
    fun nextChunk(): ByteArray?
}

interface ChunkWriter {
    fun writeChunk(chunk: ByteArray)
    fun getEmptyChunk(): ByteArray?
}

class ClosureChunkLoader(val chunkSource: () -> ByteArray?) : ChunkLoader {
    override fun nextChunk(): ByteArray? {
        return chunkSource.invoke()
    }
}

class ClosureChunkWriter(val emptyChunkSource: () -> ByteArray, val chunkWriter: (ByteArray) -> Unit) : ChunkWriter {
    override fun writeChunk(chunk: ByteArray) {
        chunkWriter.invoke(chunk)
    }

    override fun getEmptyChunk(): ByteArray {
        return emptyChunkSource.invoke()
    }

}

fun InputStream.readFullChunk(buffer: ByteArray): Int {
    var read = 0
    var pos = 0
    var rest = DatabaseConstants.CHUNK_SIZE
    while (read != -1 && rest > 0) {
        rest -= read
        pos += read
        read = this.read(buffer, pos, rest)
    }
    return pos
}

data class StreamInfo(val id: String, val md5: String, val length: BigInteger)