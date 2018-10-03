package de.mc.ladon.server.persistence.cassandra.tasks

import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.api.tasks.RepositoryTaskRunner
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * 09.05.17
 */
@Named
open class CleanupOldVersionsTaskRunner @Inject constructor(val metadataDAO: MetadataDAO,
                                                            val binaryDataDAO: BinaryDataDAO,
                                                            val taskRunner: RepositoryTaskRunner) {


    open fun cleanupVersions(repoId: String) {
        val task = CleanupOldVersionsTask(metadataDAO, binaryDataDAO, repoId)
        taskRunner.runTask(task)
    }


    open fun cleanupVersions(callContext: LadonCallContext, repoId: String, objectId: String) {
        val task = CleanupOldObjectVersionsTask(metadataDAO, binaryDataDAO, repoId, objectId)
        taskRunner.runTask(callContext, task)
    }

}