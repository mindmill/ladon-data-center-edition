package de.mc.ladon.server.core.persistence.entities.api


import java.nio.ByteBuffer

/**
 * One Chunk of Data
 * Created by Ralf Ulrich on 05.05.16.
 */
interface DataChunk {
    var chunkId: String

    var ref: String?

    var content: ByteBuffer?
}
