package de.mc.ladon.server.core.bootstrap.impl

import de.mc.ladon.server.core.api.bootstrap.BootstrapTask
import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.config.BoxConfig
import de.mc.ladon.server.core.request.impl.SystemCallContext
import javax.inject.Inject
import javax.inject.Named

/**
 * CreateSystemRepositoryTask
 * Created by ralf on 26.04.15.
 *
 */
@Named
open class CreateSystemRepoTask @Inject constructor(val repoDao: RepositoryDAO) : BootstrapTask {


    override fun shouldRun(): Boolean {
        return repoDao.getRepository(SystemCallContext(), BoxConfig.SYSTEM_REPO) == null
    }

    override fun run() {
        repoDao.addRepository(SystemCallContext(), BoxConfig.SYSTEM_REPO)
    }

    override fun isFatal(): Boolean {
        return true
    }
}