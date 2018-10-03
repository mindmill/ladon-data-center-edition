package de.mc.ladon.server.core.api.persistence.entities

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


    fun key(): ResourceKey
    fun content(): ContentMetadata
    fun properties(): PropertyMetadata

    fun isDeleted(): Boolean

}