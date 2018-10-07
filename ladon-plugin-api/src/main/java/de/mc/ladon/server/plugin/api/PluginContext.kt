package de.mc.ladon.server.plugin.api

import de.mc.ladon.server.core.api.persistence.Database
import de.mc.ladon.server.core.api.persistence.dao.*

interface PluginContext {
    val binaryDataDAO: BinaryDataDAO
    val changeTokenDAO: ChangeTokenDAO
    val chunkDAO: ChunkDAO
    val metadataDAO: MetadataDAO
    val repositoryDAO: RepositoryDAO
    val userRoleDAO: UserRoleDAO
    val database: Database
    val ctx: Map<String, Any>
}