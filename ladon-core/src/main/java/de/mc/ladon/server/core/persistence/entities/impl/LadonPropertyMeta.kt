package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.MetadataExtension
import de.mc.ladon.server.core.api.persistence.entities.PropertyMetadata
import java.util.*


/**
 * @author Ralf Ulrich
 * 11.09.16
 */
data class LadonPropertyMeta(override val content: MutableMap<String, String> = HashMap()) : PropertyMetadata {


    override fun copy(): MetadataExtension<MutableMap<String, String>> {
        return LadonPropertyMeta(this.content)
    }

    override operator fun get(key: String): String? = content[key]

    override operator fun set(key: String, value: String) {
        content[key] = value
    }

    override fun isTransient(): Boolean {
        return false
    }

    override fun putAllIfAbsent(props: PropertyMetadata) {
        props.content.forEach {
            if (!content.containsKey(it.key)) {
                content[it.key] = it.value
            }
        }
    }
}

