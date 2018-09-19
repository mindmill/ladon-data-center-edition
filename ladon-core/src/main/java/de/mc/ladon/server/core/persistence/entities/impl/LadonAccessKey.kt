package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.persistence.entities.api.AccessKey

/**
 * @author Ralf Ulrich
 * 22.10.16
 */
data class LadonAccessKey (override val accessKeyId: String, override val secretKey: String) : AccessKey