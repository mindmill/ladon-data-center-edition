package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.User

/**
 * @author Ralf Ulrich
 * on 21.08.16.
 */
class LadonUser(val username: String,
                override val password: String? = null,
                override val isEnabled: Boolean = true,
                override val roles: Set<String> = setOf()) : User {
    override fun getName() = username
}