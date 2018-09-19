package de.mc.ladon.server.core.persistence.entities.api

/**
 * @author Ralf Ulrich
 * 27.08.16
 */
interface MetadataExtension<out T> {

    fun copy(): MetadataExtension<T>

    fun  isTransient() : Boolean
}