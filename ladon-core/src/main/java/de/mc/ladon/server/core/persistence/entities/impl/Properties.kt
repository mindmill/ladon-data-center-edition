package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.persistence.entities.api.MetadataExtension
import java.util.*

/**
 * @author Ralf Ulrich
 * 11.09.16
 */
data class Properties(val content: MutableMap<String, String> = HashMap()) : MetadataExtension<MutableMap<String, String>> {


    override fun copy(): MetadataExtension<MutableMap<String, String>> {
        return Properties(this.content)
    }

    @Suppress("UNCHECKED_CAST")
    fun getProperty(key: String): String? {
        return content[key]
    }

    operator fun get(key: String): String? = content[key]


    operator fun set(key: String, value: String) {
        content[key] = value
    }

    fun putAll(properties: Properties) {
        content.putAll(properties.content)
    }

    override fun isTransient(): Boolean {
        return false
    }

    fun putAllIfAbsent(props: Properties) {
        props.content.forEach {
            if (!content.containsKey(it.key)) {
                content.put(it.key, it.value)
            }
        }
    }
}