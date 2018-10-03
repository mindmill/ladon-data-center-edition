/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.impl

import com.datastax.driver.core.utils.Bytes
import com.datastax.driver.core.utils.UUIDs
import de.mc.ladon.server.core.api.exceptions.LadonIllegalArgumentException
import de.mc.ladon.server.core.api.exceptions.LadonUnsupportedOperationException
import de.mc.ladon.server.core.api.persistence.dao.*
import de.mc.ladon.server.core.api.persistence.dao.DatabaseConstants.CHUNK_SIZE
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.persistence.cassandra.dao.api.StatementCache
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbContent
import java.io.InputStream
import java.math.BigInteger
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * DAO for storing binary data in cassandra
 * Created by Ralf Ulrich on 31.01.15.
 */
@Named
open class BinaryDataDAOImpl @Inject constructor(val mm: MappingManagerProvider,
                                                 val chunkDao: ChunkDAO,
                                                 val dbQuery: StatementCache) : BinaryDataDAO {

    //   val LOG = LoggerFactory.getLogger(javaClass)
    val mapper = mm.getMapper(DbContent::class.java)


    /**
     *
     */
    override fun getContentStream(cc: LadonCallContext, repoId: String, streamId: String, offset: BigInteger?, length: BigInteger?): InputStream? {
        if (offset ?: BigInteger.ZERO > BigInteger.ZERO) throw LadonIllegalArgumentException("offset and length is currently not supported")

        val hashList = getChunkList(repoId, streamId)
        val iter = hashList.iterator()
        return ChunkInputStream(ClosureChunkLoader { if (iter.hasNext()) chunkDao.getChunk(iter.next(), streamId) else null })

    }


    private fun getChunkList(repoId: String, streamId: String): MutableList<String> {
        val hashList = dbQuery.executePrepared("SELECT CHUNKID FROM LADON.CONTENT WHERE REPOID = :repoid and STREAMID = :streamId ",
                { rs -> rs.all().map { r -> r.getString(0) }.toMutableList() }, repoId, streamId)
        return hashList
    }


    /**
     *
     */
    override fun saveContentStream(cc: LadonCallContext, repoId: String, contentStream: InputStream?): StreamInfo {
        val newStreamId = UUIDs.timeBased().toString()
        // this set is just for cleanup in case of an error. since references on same chunk hashes are counted only once,
        // we can use a hashset to eliminate duplicate entries
        val hashSet = hashSetOf<String>()
        return try {
            val buffer = ByteArray(CHUNK_SIZE)
            val md = MessageDigest.getInstance("MD5")
            val stream = DigestInputStream(contentStream, md)
            var r = 0
            var length = 0L
            var chunkindex = 0L
            do (run {
                r = stream.readFullChunk(buffer)
                length += r
                if (r > 0) {
                    val chunkBytes = Arrays.copyOf(buffer, r)
                    val hash = chunkDao.saveChunk(chunkBytes, newStreamId)
                    hashSet.add(hash)
                    mapper.value.save(DbContent(repoId, newStreamId, chunkindex++, hash))
                }
            }) while (r > 0)
            val md5 = Bytes.toHexString(stream.messageDigest.digest())
            StreamInfo(newStreamId, md5, BigInteger.valueOf(length))

        } catch (e: Exception) {
            //cleanup on error
            dbQuery.executePrepared("DELETE FROM LADON.CONTENT WHERE REPOID = :repoId AND STREAMID = :streamId", {}, repoId, newStreamId)
            hashSet.forEach { hash -> chunkDao.deleteChunk(hash, newStreamId) }
            throw e
        }
    }

    override fun copyContentStream(cc: LadonCallContext, repoId: String, streamId: String, destRepo: String): String {
        val newStreamId = UUIDs.timeBased().toString()
        val chunkList = getChunkList(repoId, streamId)
        var count = 0L
        chunkList.forEach { hash ->
            mapper.value.save(DbContent(destRepo, newStreamId, count++, hash))
            chunkDao.saveChunkRef(hash, newStreamId)
        }
        return newStreamId
    }

    /**
     *
     */
    override fun appendContentStream(cc: LadonCallContext, repoId: String, streamId: String, contentStream: InputStream?, length: BigInteger?, isLastChunk: Boolean) {
        throw LadonUnsupportedOperationException("not implemented")
    }


    /**
     *
     */
    override fun deleteContentStream(cc: LadonCallContext, repoId: String, streamId: String) {
        val hashList = getChunkList(repoId, streamId)
        dbQuery.executePrepared("DELETE FROM LADON.CONTENT WHERE REPOID = :repoId AND STREAMID = :streamId", {}, repoId, streamId)
        hashList.forEach { hash -> chunkDao.deleteChunk(hash, streamId) }

    }


    fun InputStream.readFullChunk(buffer: ByteArray): Int {
        var read = 0
        var pos = 0
        var rest = CHUNK_SIZE
        while (read != -1 && rest > 0) {
            rest -= read
            pos += read
            read = this.read(buffer, pos, rest)
        }
        return pos
    }
}