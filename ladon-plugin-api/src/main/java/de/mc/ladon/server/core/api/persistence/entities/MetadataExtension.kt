package de.mc.ladon.server.core.api.persistence.entities

/**
 * @author Ralf Ulrich
 * 27.08.16
 */
interface MetadataExtension<out T> {

    fun copy(): MetadataExtension<T>

    fun  isTransient() : Boolean
}