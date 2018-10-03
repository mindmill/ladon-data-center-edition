package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.MetadataExtension

/**
 * @author Ralf Ulrich
 * 11.09.16
 */
data class Acl(val content: List<Ace> = arrayListOf()) : MetadataExtension<List<Ace>> {

    override fun isTransient(): Boolean {
        return false
    }

    override fun copy(): MetadataExtension<List<Ace>> {
        return Acl(content)
    }
}