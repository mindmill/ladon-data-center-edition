package de.mc.ladon.server.core.persistence.services.impl

import de.mc.ladon.server.core.exceptions.LadonIllegalArgumentException
import de.mc.ladon.server.core.exceptions.LadonObjectNotFoundException
import de.mc.ladon.server.core.persistence.dao.api.UserRoleDAO
import de.mc.ladon.server.core.persistence.entities.api.User
import de.mc.ladon.server.core.persistence.services.api.LadonUserDetailsManager
import javax.inject.Inject
import javax.inject.Named

/**
 * @author  Ralf Ulrich on 17.01.16.
 */
@Named
open class LadonUserDetailsManagerImpl
@Inject
constructor(val userRoleDAO: UserRoleDAO) : LadonUserDetailsManager {


    override fun loadUserByUsername(name: String): User {
        val dbUser = userRoleDAO.getUser(name) ?: throw LadonObjectNotFoundException("no user with name $name found")
        return dbUser
    }

    override fun userExists(name: String): Boolean {
        return userRoleDAO.getUser(name) !== null
    }

    override fun updateUser(user: User) {
        val userid = user.name
        val dbUser = userRoleDAO.getUser(userid) ?: throw LadonObjectNotFoundException("no user with name ${user.name} found")
        val oldRoles = dbUser.roles

        userRoleDAO.addUser(user.name, dbUser.password!!, user.isEnabled, user.roles)
        val removedRoles = oldRoles.filter { !(user.roles.contains(it)) }
        val addedRoles = user.roles.filter { !(oldRoles.contains(it)) }
        removedRoles.forEach {
            userRoleDAO.deleteRole(it, userid)
        }
        addedRoles.forEach {
            userRoleDAO.addRole(it, userid)
        }
        userRoleDAO.updateRolesForKeys(userid)
    }

    override fun createUser(user: User) {
        val userid = user.name
        // val dbUser = userRoleDAO.getUser(userid)
        // if (dbUser != null) throw IllegalArgumentException("user with name ${user.name} already exists")
        if (user is User)
            userRoleDAO.addUser(user.name!!, user.password!!, user.isEnabled, user.roles)
        else throw IllegalArgumentException("user is no dbuser instance")

        user.roles.forEach {
            userRoleDAO.addRole(it, user.name!!)
        }
        userRoleDAO.addNewKey(userid)
    }

    override fun deleteUser(name: String) {
        val dbUser = userRoleDAO.getUser(name) ?: throw LadonObjectNotFoundException("no user with name $name found")
        dbUser.roles.forEach {
            userRoleDAO.deleteRole(it, name)
        }
        userRoleDAO.getKeysForUser(name).forEach {
            userRoleDAO.deleteKey(it.first)
        }
    }

    override fun changePassword(name: String, oldPassword: String, newPassword: String) {
        val dbUser = userRoleDAO.getUser(name) ?: throw LadonObjectNotFoundException("no user with name $name found")
        if (dbUser.password != oldPassword) throw LadonIllegalArgumentException("Wrong Password")
        userRoleDAO.addUser(dbUser.name, newPassword, dbUser.isEnabled, dbUser.roles)
    }
}