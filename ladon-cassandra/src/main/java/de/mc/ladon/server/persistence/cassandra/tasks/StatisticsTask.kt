/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.tasks

import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.api.tasks.RepositoryTask
import de.mc.ladon.server.core.config.BoxConfig.Companion.SYSTEM_REPO
import de.mc.ladon.server.core.persistence.entities.impl.LadonContentMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonMetadata
import de.mc.ladon.server.core.persistence.entities.impl.LadonPropertyMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonResourceKey
import de.mc.ladon.server.core.util.ByteFormat
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.util.*

/**
 * Run and collect number of buckets as well as number of files
 */
class StatisticsTask(
        val repositoryDAO: RepositoryDAO,
        val metadataDAO: MetadataDAO,
        val binaryDataDAO: BinaryDataDAO) : RepositoryTask<Boolean> {

    val log = LoggerFactory.getLogger(javaClass)

    fun Long.humanReadable() = ByteFormat.humanReadableByteCount(this, true)

    override fun run(cc: LadonCallContext): Boolean {
        log.info("Start collecting statistics")

        try {


            val repos = repositoryDAO.getRepositories(cc).toList()
            log.info("Found ${repos.size} bucket")

            val sizePerBucket: MutableMap<String, Size> = mutableMapOf()
            repos.forEach {
                sizePerBucket[it.repoId!!] = metadataDAO.listAllMetadata(cc,
                        it.repoId!!,
                        "",
                        null,
                        null,
                        Int.MAX_VALUE,
                        false)
                        .first
                        .first
                        .fold(0L to 0L) { sum, meta -> sum.first + 1 to sum.second + meta.content().length }


            }
            val content = ByteArrayOutputStream()
            content.use {
                PrintWriter(it).apply {
                    println("Ladon Repository Stats ${Date()}\n")
                    println("Buckets: ${sizePerBucket.size}, total size: ${sizePerBucket.map { it.value.second }.sum().humanReadable()}\n")
                    sizePerBucket.forEach { repo, size ->
                        println("$repo:  Files: ${size.first} Size: ${size.second.humanReadable()} ")
                    }
                }
            }
            val info = binaryDataDAO.saveContentStream(cc, SYSTEM_REPO, ByteArrayInputStream(content.toByteArray()))

            val meta = LadonMetadata()
            meta.set(LadonContentMeta(info.id, info.md5, content.size().toLong(), createdBy = "System"))
            meta.set(LadonPropertyMeta(mutableMapOf("content-type" to "text/plain")))
            metadataDAO.saveMetadata(cc, LadonResourceKey(SYSTEM_REPO, "stats/buckets.txt", cc.getCallId().id()), meta)
        } catch (e: Exception) {
            log.error("Collecting failed", e)
        }
        log.info("end collecting statistics")
        return true
    }

}
typealias  Size = Pair<Long, Long>
