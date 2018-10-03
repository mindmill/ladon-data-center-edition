/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.tasks

import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.api.tasks.RepositoryTask
import de.mc.ladon.server.core.persistence.entities.impl.LadonHistoryKey
import org.slf4j.LoggerFactory

/**
 * If the Repository is set unversioned,
 * this task runs and cleans all old data
 */
class CleanupOldObjectVersionsTask(
        val metadataDAO: MetadataDAO,
        val binaryDataDAO: BinaryDataDAO,
        val repoId: String, val objectId: String) : RepositoryTask<Boolean> {

    val log = LoggerFactory.getLogger(javaClass)


    override fun run(callContext: LadonCallContext): Boolean {
        log.info("Start cleaning old versions for object $repoId : $objectId")
        val list = metadataDAO.getMetadataHistory(callContext, LadonHistoryKey(repoId, objectId))
        val first = list.firstOrNull()
        val rest = list.drop(1)

        if (first == null) {
            log.error("metadata not found for key $repoId, $objectId")
            return false
        }

        if (first.isDeleted()) {
            log.info("user ${callContext.getUser().name} deleting last version of $repoId,  $objectId ")
            log.info("user ${callContext.getUser().name} deleting content id ${first.content().id}")
            binaryDataDAO.deleteContentStream(callContext, repoId, first.content().id)
            metadataDAO.removeMetadata(callContext, first.key())
        }

        rest.forEach {
            val contentId = it.content().id
            log.info("user ${callContext.getUser().name} overwriting version ${it.key()} ")
            log.info("user ${callContext.getUser().name} overwriting content id $contentId ")
            try {
                binaryDataDAO.deleteContentStream(callContext, repoId, contentId)
                metadataDAO.removeMetadata(callContext, it.key())
            } catch (e: Exception) {
                log.error("error during delete of $repoId, $objectId", e)
            }
        }
        return true
    }

}