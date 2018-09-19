/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.persistence.entities.api.MetadataExtension
import de.mc.ladon.server.core.request.LadonCallContext
import java.util.*

/**
 * ResourceKey
 * Created by Ralf Ulrich on 08.04.16.
 */
data class ResourceKey(val repositoryId: String,
                       val versionSeriesId: String,
                       val changeToken: UUID) : MetadataExtension<ResourceKey> {

    fun updatedKey(cc: LadonCallContext): ResourceKey {
        return ResourceKey(repositoryId, versionSeriesId, cc.getCallId().id())
    }

    fun historyKey(): HistoryKey {
        return HistoryKey(repositoryId, versionSeriesId)
    }

    fun getExternalKey(): String {
        return "$versionSeriesId:${changeToken.toString()}"
    }

    override fun copy(): MetadataExtension<ResourceKey> {
        return ResourceKey(repositoryId, versionSeriesId, changeToken)
    }

    override fun isTransient(): Boolean {
        return true
    }
}