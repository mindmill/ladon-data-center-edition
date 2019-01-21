package de.mc.ladon.server.persistence.cassandra.tasks

import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.api.tasks.RepositoryTaskRunner
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * 21.01.19
 */
@Named
open class StatisticsTaskRunner @Inject constructor(val metadataDAO: MetadataDAO,
                                                    val binaryDataDAO: BinaryDataDAO,
                                                    val repositoryDAO: RepositoryDAO,
                                                    val taskRunner: RepositoryTaskRunner) {


    open fun collectStats(cc: LadonCallContext) {
        val task = StatisticsTask(repositoryDAO,metadataDAO,binaryDataDAO)
        taskRunner.runTask(cc,task)
    }

}
