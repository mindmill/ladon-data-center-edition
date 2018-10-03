package de.mc.ladon.server.core.api.persistence.entities

import de.mc.ladon.server.core.api.request.LadonCallContext
import java.util.*

interface ResourceKey {
    val repositoryId: String
    val versionSeriesId: String
    val changeToken: UUID
    fun updatedKey(cc: LadonCallContext): ResourceKey
}