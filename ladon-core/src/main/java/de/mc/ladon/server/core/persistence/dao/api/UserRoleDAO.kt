package de.mc.ladon.server.core.persistence.dao.api

import de.mc.ladon.server.core.persistence.entities.api.AccessKey
import de.mc.ladon.server.core.persistence.entities.api.Role
import de.mc.ladon.server.core.persistence.entities.api.User

/**
 * UserRoleDAO
 * Created by Ralf Ulrich on 22.01.16.
 */
interface UserRoleDAO {

    fun getUser(userid: String): User?

    fun addUser(userid: String, password: String, enabled: Boolean, roles: Set<String>)

    fun deleteUser(userid: String)

    fun getRole(roleid: String): Role

    fun addRole(roleid: String, member: String)

    fun deleteRole(roleid: String, member: String)

    fun getAllUsers(filter: (User) -> Boolean): List<User>

    fun getAllRoleIds(filter: (String) -> Boolean): List<String>

    fun getAllMemberIds(roleid: String): List<String>

    fun getKeysForUser(userid: String): List<Pair<String,String>>

    fun getKey(accessKeyId: String): Pair<AccessKey, User>?

    fun updateRolesForKeys(userid: String)

    fun addNewKey(userid: String): Pair<String,String>

    fun deleteKey(accessKeyId : String)
}