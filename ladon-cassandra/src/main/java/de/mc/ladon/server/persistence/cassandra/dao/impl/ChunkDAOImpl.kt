/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.impl

import com.datastax.driver.core.utils.Bytes
import de.mc.ladon.server.core.api.exceptions.LadonStorageException
import de.mc.ladon.server.core.api.persistence.dao.ChunkDAO
import de.mc.ladon.server.core.api.persistence.encryption.Encryptor
import de.mc.ladon.server.persistence.cassandra.dao.api.StatementCache
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbChunk
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Named

/**
 * ChunkDAOImpl
 * Created by Ralf Ulrich on 31.01.15.
 */
@Named
open class ChunkDAOImpl @Inject constructor(mm: MappingManagerProvider, val dbQuery: StatementCache, val crypto: Encryptor) : ChunkDAO {

    val mapper = mm.getMapper(DbChunk::class.java)


    override fun saveChunk(data: ByteArray, ref: String): String {
        val hash = data.getSHA256Hash()
        val hashString = Bytes.toHexString(hash)
        mapper.value.save(DbChunk(hashString, ref, ByteBuffer.wrap(crypto.encrypt(data))))
        return hashString
    }

    override fun saveChunkRef(chunkid: String, ref: String) {
        dbQuery.executePrepared("INSERT INTO LADON.CHUNKS ( chunkid , ref) VALUES ( :chunkid , :ref )", {}, chunkid, ref)
    }

    override fun getChunk(chunkid: String, ref: String): ByteArray {

        val chunk = mapper.value.get(chunkid, ref)
        return if (chunk?.content != null) crypto.decrypt(Bytes.getArray(chunk.content)) else
            throw LadonStorageException("Chunk $chunkid with ref $ref not found")

    }

    override fun deleteChunk(chunkid: String, ref: String) {
        val refs = dbQuery.executePrepared("SELECT CHUNKID, REF FROM LADON.CHUNKS WHERE CHUNKID = :chunkid",
                { rs -> rs.all().map { it.getString("ref") }.toSet() }, chunkid)
        if (ref in refs && refs.size == 1) {
            dbQuery.executePrepared("DELETE FROM LADON.CHUNKS WHERE CHUNKID = :chunkid", {}, chunkid)
        } else {
            mapper.value.delete(chunkid, ref)
        }

    }

    fun ByteArray.getSHA256Hash(): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(this)
    }

}