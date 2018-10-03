/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.ContentMetadata
import de.mc.ladon.server.core.api.persistence.entities.MetadataExtension
import java.util.*

/**
 * Extension for binary content metadata
 */
data class LadonContentMeta(override val id: String,
                            override val hash: String,
                            override val length: Long,
                            override val created: Date = Date(),
                            override val createdBy: String,
                            override val deleted: Date? = null,
                            override val deletedBy: String? = null) : ContentMetadata {
    override fun copy(): MetadataExtension<ContentMetadata> {
        return LadonContentMeta(id, hash, length, created, createdBy, deleted, deletedBy)
    }

    override fun isTransient(): Boolean {
        return false
    }
}