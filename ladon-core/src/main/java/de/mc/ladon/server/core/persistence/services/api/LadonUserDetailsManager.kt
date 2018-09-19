package de.mc.ladon.server.core.persistence.services.api

import de.mc.ladon.server.core.persistence.entities.api.User

/**
 * LadonUserDetailsManager
 * Created by Ralf Ulrich on 17.01.16.
 */
interface LadonUserDetailsManager {
    fun loadUserByUsername(name: String): User
    fun userExists(name: String): Boolean
    fun updateUser(user: User)
    fun createUser(user: User)
    fun deleteUser(name: String)
    fun changePassword(name: String, oldPassword: String, newPassword: String)
}