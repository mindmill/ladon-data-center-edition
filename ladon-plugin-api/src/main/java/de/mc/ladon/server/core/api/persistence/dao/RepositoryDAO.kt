/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.api.persistence.dao

import de.mc.ladon.server.core.api.persistence.entities.Repository
import de.mc.ladon.server.core.api.request.LadonCallContext

/**
 * RepositoryDAO
 * Created by Ralf Ulrich on 24.04.15.
 */
interface RepositoryDAO {

    fun getRepositories(callContext: LadonCallContext): List<Repository>

    fun getRepository(callContext: LadonCallContext, repoId: String?): Repository?

    fun addRepository(callContext: LadonCallContext, repoId: String)

    fun setVersioned(callContext: LadonCallContext, repoId: String, versioned: Boolean)

    fun deleteRepository(callContext: LadonCallContext, repoId: String)

}