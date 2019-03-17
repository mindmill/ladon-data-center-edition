/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.tasks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.ChangeTokenDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.api.tasks.RepositoryTask
import de.mc.ladon.server.core.persistence.entities.impl.LadonContentMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonMetadata
import de.mc.ladon.server.core.persistence.entities.impl.LadonPropertyMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonResourceKey
import de.mc.ladon.server.core.util.ByteFormat
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

/**
 * Run and collect number of buckets as well as number of files
 */
class PathParserTask(
        val repositoryDAO: RepositoryDAO,
        val metadataDAO: MetadataDAO,
        val binaryDataDAO: BinaryDataDAO) : RepositoryTask<Boolean> {

    val log = LoggerFactory.getLogger(javaClass)

    fun Long.humanReadable() = ByteFormat.humanReadableByteCount(this, true)

    override fun run(cc: LadonCallContext): Boolean {
        log.info("Start collecting Path Infos")
        val name = "stats/pathinfo.txt"

        try {


            val repos = repositoryDAO.getRepositories(cc).filterNot { it.repoId!!.startsWith("_") }.toList()
            log.info("Found ${repos.size} bucket")




            repos.forEach {
                val metaName = "_" + it.repoId
                var startVersion = ""
                val metaRepo = repositoryDAO.getRepository(cc, metaName)
                if (metaRepo == null) {
                    repositoryDAO.addRepository(cc, metaName)
                }

               val allPaths = metadataDAO.listAllMetadata(cc,it.repoId!!,"",null,null,Int.MAX_VALUE)
                        .first.first.map { it.key().versionSeriesId }


            }




        } catch (e: Exception) {
            log.error("Collecting failed", e)
        }
        log.info("end collecting statistics")
        return true
    }

    val mapper = jacksonObjectMapper()

    private fun writeFile(cc: LadonCallContext, bucket: String, name: String, content: Any) {
        val content = ByteArrayOutputStream()
        content.use {
            PrintWriter(it).apply {
                mapper.writeValue(it, content)
            }
        }
        val info = binaryDataDAO.saveContentStream(cc, bucket, ByteArrayInputStream(content.toByteArray()))
        val meta = LadonMetadata()
        meta.set(LadonContentMeta(info.id, info.md5, content.size().toLong(), createdBy = "System"))
        meta.set(LadonPropertyMeta(mutableMapOf("content-type" to "text/plain")))
        metadataDAO.saveMetadata(cc, LadonResourceKey(bucket, name, cc.getCallId().id()), meta)
    }


    data class PathInfo(val lastVersion: String, val elements: HashMap<String, Any> = hashMapOf())
}
