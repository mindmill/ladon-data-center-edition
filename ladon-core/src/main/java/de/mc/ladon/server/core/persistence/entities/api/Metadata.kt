package de.mc.ladon.server.core.persistence.entities.api

import de.mc.ladon.server.core.persistence.entities.impl.Content
import de.mc.ladon.server.core.persistence.entities.impl.Properties
import de.mc.ladon.server.core.persistence.entities.impl.ResourceKey
import kotlin.reflect.KClass

/**
 *
 * @author Ralf Ulrich
 * 27.08.16
 */
interface Metadata {

    operator fun <T : MetadataExtension<*>> get(ext: KClass<T>): T?

    fun set(data: MetadataExtension<*>)

    fun <T : MetadataExtension<*>> remove(ext: KClass<T>): T?

    fun copy(): Metadata


    fun key(): ResourceKey {
        return this[ResourceKey::class]!!
    }

    fun content(): Content {
        return this[Content::class]!!
    }

    fun properties(): Properties {
        return this[Properties::class]!!
    }

    fun isDeleted(): Boolean {
        return content().deleted != null
    }


}