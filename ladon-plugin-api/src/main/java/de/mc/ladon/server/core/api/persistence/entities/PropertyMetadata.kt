package de.mc.ladon.server.core.api.persistence.entities

interface PropertyMetadata : MetadataExtension<MutableMap<String, String>> {
    operator fun get(key: String): String?
    operator fun set(key: String, value: String)
    fun putAllIfAbsent(props: PropertyMetadata)
    val content: MutableMap<String,String>
}
