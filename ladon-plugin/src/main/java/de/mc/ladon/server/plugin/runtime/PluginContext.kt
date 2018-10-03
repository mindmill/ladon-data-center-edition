package de.mc.ladon.server.plugin.runtime


import de.mc.ladon.server.core.api.persistence.Database
import de.mc.ladon.server.core.api.persistence.dao.*

import javax.inject.Inject
import javax.inject.Named

@Named
class PluginContext @Inject
constructor(val binaryDataDAO: BinaryDataDAO,
            val changeTokenDAO: ChangeTokenDAO,
            val chunkDAO: ChunkDAO,
            val metadataDAO: MetadataDAO,
            val repositoryDAO: RepositoryDAO,
            val userRoleDAO: UserRoleDAO,
            val database: Database) {
    val ctx: Map<String, Any> = hashMapOf()
}
