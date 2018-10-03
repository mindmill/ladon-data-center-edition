package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.Metadata
import de.mc.ladon.server.core.api.persistence.entities.MetadataExtension
import de.mc.ladon.server.core.api.persistence.entities.ResourceKey
import java.util.*
import kotlin.reflect.KClass

/**
 * @author Ralf Ulrich
 * 27.08.16
 */
data class LadonMetadata(val content: MutableMap<String, MetadataExtension<*>> = HashMap()) : Metadata {
    override fun copy(): Metadata {
        val clone = LadonMetadata()
        for (k in content.values) {
            clone.set(k.copy())
        }
        return clone
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : MetadataExtension<*>> remove(ext: KClass<T>): T? {
        return content.remove(ext.java.simpleName) as T?
    }

    override fun set(data: MetadataExtension<*>) {
        content[data.javaClass.simpleName] = data
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : MetadataExtension<*>> get(ext: KClass<T>): T? {
        return content[ext.java.simpleName] as T?
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is LadonMetadata -> {
                val key1 = get(LadonResourceKey::class)
                val key2 = other.get(LadonResourceKey::class)
                if (key1 != null && key2 != null) key1 == key2
                else super.equals(other)
            }
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return get(LadonResourceKey::class)?.hashCode() ?:
                super.hashCode()
    }

    override fun key(): ResourceKey {
        return this[LadonResourceKey::class]!!
    }

    override fun content(): LadonContentMeta {
        return this[LadonContentMeta::class]!!
    }

    override fun properties(): LadonPropertyMeta {
        return this[LadonPropertyMeta::class]!!
    }

    override fun isDeleted(): Boolean {
        return content().deleted != null
    }
}







