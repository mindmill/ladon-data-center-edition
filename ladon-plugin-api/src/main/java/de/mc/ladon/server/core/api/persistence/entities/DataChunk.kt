package de.mc.ladon.server.core.api.persistence.entities


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
