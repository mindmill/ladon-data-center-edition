/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.tasks

import de.mc.ladon.server.core.persistence.dao.api.BinaryDataDAO
import de.mc.ladon.server.core.persistence.dao.api.MetadataDAO
import de.mc.ladon.server.core.request.LadonCallContext
import de.mc.ladon.server.core.tasks.api.RepositoryTask
import org.slf4j.LoggerFactory

/**
 * If the Repository has been changed from versioned to unversioned,
 * this task runs and deletes all versions but the current
 */
class CleanupOldVersionsTask(
        val metadataDAO: MetadataDAO,
        val binaryDataDAO: BinaryDataDAO,
        val repoId: String) : RepositoryTask<Boolean> {

    val log = LoggerFactory.getLogger(javaClass)


    override fun run(callContext: LadonCallContext): Boolean {
        log.info("Start cleaning old versions for repository $repoId")

        val (list, _) = metadataDAO.listAllMetadata(callContext, repoId, "", null, Int.MAX_VALUE, true)
        val uniqueIds = mutableMapOf<String, String>()
        // first reduce all to the last version only, delete all others
        list.forEach {
            try {
                val versionSeriesId = it.key().versionSeriesId
                val contentId = it.content().id
                if (versionSeriesId !in uniqueIds) {
                    uniqueIds.put(versionSeriesId, contentId)
                    // then check again, if the latest version is a delete marker, delete it as well
                    if (it.isDeleted()) {
                        log.info("deleting last version of ${it.key()} ")
                        binaryDataDAO.deleteContentStream(callContext, repoId, contentId)
                        metadataDAO.removeMetadata(callContext, it.key())
                    }
                } else {
                    log.info("deleting version ${it.key()} ")
                    if (contentId != uniqueIds[versionSeriesId]) {
                        log.info("deleting content id $contentId ")
                        binaryDataDAO.deleteContentStream(callContext, repoId, contentId)
                    }
                    metadataDAO.removeMetadata(callContext, it.key())
                }
            } catch (e: Exception) {
                log.error("error during delete", e)
            }
        }
        log.info("end cleaning old versions for repository $repoId")
        return true
    }

}