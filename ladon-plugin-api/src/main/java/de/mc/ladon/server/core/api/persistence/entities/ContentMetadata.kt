package de.mc.ladon.server.core.api.persistence.entities

import java.util.*

interface ContentMetadata : MetadataExtension<ContentMetadata> {
    val id: String
    val hash: String
    val length: Long
    val created: Date
    val createdBy: String
    val deleted: Date?
    val deletedBy: String?
}