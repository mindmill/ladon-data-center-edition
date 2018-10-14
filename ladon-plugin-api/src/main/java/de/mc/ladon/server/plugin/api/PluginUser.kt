package de.mc.ladon.server.plugin.api

import de.mc.ladon.server.core.api.persistence.entities.User

/**
 * @author Ralf Ulrich
 * on 21.08.16.
 */
class PluginUser(val username: String,
                 override val password: String? = null,
                 override val isEnabled: Boolean = true,
                 override val roles: Set<String> = setOf()) : User {
    override fun getName() = username
}