/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.dao.api

/**
 * ChunkDAO
 * Created by Ralf Ulrich on 31.01.15.
 */
interface ChunkDAO {

    /**
     * save the binary data of this chunk. if a ref is given it'll be saved , if not it will only incremen the refcount.
     * @return the id of this chunk
     */
    fun saveChunk(data: ByteArray, ref: String): String

    /**
     * save a new ref to a given chunk
     */
    fun saveChunkRef(chunkid: String, ref: String)

    /**
     * returns the binary data of the chunk or throws exception if not present
     */
    fun getChunk(chunkid: String, ref: String): ByteArray

    /**
     * deletes the chunk with the given chunkid.
     * Also removes the ref from the ref list, if no ref is given it only decrements the refcount.
     */
    fun deleteChunk(chunkid: String, ref: String)
}