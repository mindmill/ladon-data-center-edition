/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.persistence.entities.api.MetadataExtension
import java.util.*

/**
 * Extension for binary content metadata
 */
data class Content(val id: String, val hash: String, val length: Long, val created: Date = Date(), val createdBy: String, val deleted: Date? = null, val deletedBy: String? = null) : MetadataExtension<Content> {
    override fun copy(): MetadataExtension<Content> {
        return Content(id, hash, length, created, createdBy, deleted, deletedBy)
    }

    override fun isTransient(): Boolean {
        return false
    }
}