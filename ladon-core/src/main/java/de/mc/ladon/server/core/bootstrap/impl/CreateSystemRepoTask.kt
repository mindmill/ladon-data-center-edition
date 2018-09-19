package de.mc.ladon.server.core.bootstrap.impl

import de.mc.ladon.server.core.bootstrap.api.BootstrapTask
import de.mc.ladon.server.core.config.BoxConfig
import de.mc.ladon.server.core.persistence.dao.api.RepositoryDAO
import de.mc.ladon.server.core.request.SystemCallContext

/**
 * CreateSystemRepositoryTask
 * Created by ralf on 26.04.15.
 */ open class CreateSystemRepoTask  constructor(val repoDao: RepositoryDAO) : BootstrapTask {


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