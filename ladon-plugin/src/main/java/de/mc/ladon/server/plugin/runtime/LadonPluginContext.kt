package de.mc.ladon.server.plugin.runtime


import de.mc.ladon.server.core.api.persistence.Database
import de.mc.ladon.server.core.api.persistence.dao.*
import de.mc.ladon.server.plugin.api.PluginContext

import javax.inject.Inject
import javax.inject.Named

@Named
class LadonPluginContext @Inject
constructor(override val binaryDataDAO: BinaryDataDAO,
            override val changeTokenDAO: ChangeTokenDAO,
            override val chunkDAO: ChunkDAO,
            override val metadataDAO: MetadataDAO,
            override val repositoryDAO: RepositoryDAO,
            override val userRoleDAO: UserRoleDAO,
            override val database: Database) : PluginContext {
    override val ctx: Map<String, Any> = hashMapOf()
}
