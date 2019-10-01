/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.mc.ladon.cmis

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException
import java.util.*

/**
 * Manages all repositories.
 */
class LadonCmisRepositoryManager {

    private val repositories: MutableMap<String, LadonCmisRepository>

    init {
        repositories = HashMap()
    }


    fun addRepository(fsr: LadonCmisRepository?) {
        if (fsr == null || fsr.repositoryId == null) {
            return
        }

        repositories[fsr.repositoryId] = fsr
    }

    /**
     * Gets a repository object by id.
     */
    fun getRepository(repositoryId: String): LadonCmisRepository {

        return repositories[repositoryId]
                ?: throw CmisObjectNotFoundException("Unknown repository '$repositoryId'!")
    }

    /**
     * Returns all repository objects.
     */
    fun getRepositories(): Collection<LadonCmisRepository> {
        return repositories.values
    }


}
