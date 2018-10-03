/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.impl

import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.api.persistence.entities.Repository
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.persistence.cassandra.dao.api.RepositoryAccessor
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbRepository
import de.mc.ladon.server.persistence.cassandra.tasks.CleanupOldVersionsTaskRunner
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * RepositoryDAOImpl
 * Created by Ralf Ulrich on 24.04.15.
 */
@Named
open class RepositoryDAOImpl @Inject constructor(mm: MappingManagerProvider, val deleteOldVersions: CleanupOldVersionsTaskRunner) : RepositoryDAO {

    val mapper = mm.getMapper(DbRepository::class.java)
    val accessor = mm.getAccessor(RepositoryAccessor::class.java)

    override fun getRepositories(callContext: LadonCallContext): List<Repository> {
        return accessor.value.listRepositories().all()
    }

    override fun setVersioned(callContext: LadonCallContext, repoId: String, versioned: Boolean) {
        val repo = getRepository(callContext, repoId)
        val cleanup = repo?.versioned != versioned && !versioned
        repo?.versioned = versioned
        if (repo != null && repo is DbRepository) mapper.value.save(repo)
        if (cleanup) deleteOldVersions.cleanupVersions(repoId)
    }

    override fun addRepository(callContext: LadonCallContext, repoId: String) {
        mapper.value.save(DbRepository(repoId = repoId, creationdate = Date(), createdby = callContext.getUser().name, versioned = true))
    }

    override fun deleteRepository(callContext: LadonCallContext, repoId: String) {
        val repo = mapper.value.get(repoId)
                ?: throw IllegalArgumentException("can't delete repository, no repo with id $repoId found ")
        mapper.value.delete(repo)
    }


    override fun getRepository(callContext: LadonCallContext, repoId: String?): Repository? {
        return mapper.value.get(repoId)
    }


}