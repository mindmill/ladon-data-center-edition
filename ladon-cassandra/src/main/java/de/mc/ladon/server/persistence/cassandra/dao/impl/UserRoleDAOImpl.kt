package de.mc.ladon.server.persistence.cassandra.dao.impl

import de.mc.ladon.server.core.persistence.dao.api.UserRoleDAO
import de.mc.ladon.server.core.persistence.entities.api.AccessKey
import de.mc.ladon.server.core.persistence.entities.api.Role
import de.mc.ladon.server.core.persistence.entities.api.User
import de.mc.ladon.server.core.persistence.entities.impl.LadonAccessKey
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import de.mc.ladon.server.core.persistence.services.api.S3KeyGen
import de.mc.ladon.server.persistence.cassandra.dao.api.UserRoleAccessor
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import de.mc.ladon.server.persistence.cassandra.encryption.api.Encryptor
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbAccessKey
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbRole
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbUser
import javax.inject.Inject
import javax.inject.Named

/**
 * UserRoleDAOImpl
 * Created by Ralf Ulrich on 22.01.16.
 */
@Named
open class UserRoleDAOImpl
@Inject
constructor( mm: MappingManagerProvider, val keygen: S3KeyGen, val crypto: Encryptor) : UserRoleDAO {

    val userMapper = mm.getMapper(DbUser::class.java)
    val roleMapper = mm.getMapper(DbRole::class.java)
    val keyMapper = mm.getMapper(DbAccessKey::class.java)
    val userRoleAccessor = mm.getAccessor(UserRoleAccessor::class.java)


    override fun getUser(userid: String): User? {
        return userMapper.value.get(userid)
    }

    override fun addUser(userid: String, password: String, enabled: Boolean, roles: Set<String>) {
        userMapper.value.save(DbUser(userid, password, enabled, roles))
    }

    override fun deleteUser(userid: String) {
        userMapper.value.delete(userid)
    }

    override fun getRole(roleid: String): Role {
        return roleMapper.value.get(roleid)
    }

    override fun addRole(roleid: String, member: String) {
        roleMapper.value.save(DbRole(roleid, member))
    }

    override fun deleteRole(roleid: String, member: String) {
        roleMapper.value.delete(roleid, member)
    }

    override fun getAllMemberIds(roleid: String): List<String> {
        return userRoleAccessor.value.getAllMembers(roleid).map { it.member!! }
    }

    override fun getAllUsers(filter: (User) -> Boolean): List<User> {
        return userRoleAccessor.value.getAllUsers().filter { filter.invoke(it) }
    }

    override fun getAllRoleIds(filter: (String) -> Boolean): List<String> {
        return userRoleAccessor.value.getAllRoles().map { it.getString("roleid") }.filter { filter.invoke(it) }
    }

    override fun getKeysForUser(userid: String): List<Pair<String, String>> {
        return userRoleAccessor.value.getAllKeys().filter { it.userid == userid }
                .map { Pair(it.accessKeyId!!, crypto.decrypt(it.secretKey!!)) }
    }

    override fun updateRolesForKeys(userid: String) {
        val roles = getUser(userid)?.roles
        userRoleAccessor.value.getAllKeys().filter { it.userid == userid }.forEach {
            keyMapper.value.save(it.copy(roles = roles))
        }
    }

    override fun getKey(accessKeyId: String): Pair<AccessKey, User>? {
        val key = keyMapper.value.get(accessKeyId)
        return if (key != null) {
            Pair(LadonAccessKey(key.accessKeyId!!, crypto.decrypt(key.secretKey!!)), LadonUser(key.userid!!, null, true, key.roles!!))
        } else null
    }

    override fun addNewKey(userid: String): Pair<String, String> {
        val keys = keygen.generateNewKey()
        val user = getUser(userid)
        if (user != null)
            keyMapper.value.save(DbAccessKey(keys.publicKey, crypto.encrypt(keys.privateKey), userid, user.roles))
        return Pair(keys.publicKey, keys.privateKey)
    }

    override fun deleteKey(accessKeyId: String) {
        keyMapper.value.delete(accessKeyId)
    }
}