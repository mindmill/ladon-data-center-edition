package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.persistence.entities.api.Metadata
import de.mc.ladon.server.core.persistence.entities.api.MetadataExtension
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
                val key1 = get(ResourceKey::class)
                val key2 = other.get(ResourceKey::class)
                if (key1 != null && key2 != null) key1.equals(key2)
                else super.equals(other)
            }
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return get(ResourceKey::class)?.hashCode() ?:
                super.hashCode()
    }
}







