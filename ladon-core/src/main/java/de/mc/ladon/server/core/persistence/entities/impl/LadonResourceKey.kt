/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.MetadataExtension
import de.mc.ladon.server.core.api.persistence.entities.ResourceKey
import de.mc.ladon.server.core.api.request.LadonCallContext
import java.util.*

/**
 * LadonResourceKey
 * Created by Ralf Ulrich on 08.04.16.
 */
data class LadonResourceKey(override val repositoryId: String,
                            override val versionSeriesId: String,
                            override val changeToken: UUID) : MetadataExtension<LadonResourceKey>, ResourceKey {

    override fun updatedKey(cc: LadonCallContext): LadonResourceKey {
        return LadonResourceKey(repositoryId, versionSeriesId, cc.getCallId().id())
    }

    fun historyKey(): LadonHistoryKey {
        return LadonHistoryKey(repositoryId, versionSeriesId)
    }

    fun getExternalKey(): String {
        return "$versionSeriesId:${changeToken.toString()}"
    }

    override fun copy(): MetadataExtension<LadonResourceKey> {
        return LadonResourceKey(repositoryId, versionSeriesId, changeToken)
    }

    override fun isTransient(): Boolean {
        return true
    }
}