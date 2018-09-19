/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.api

import com.datastax.driver.core.ResultSet
import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Query
import com.datastax.driver.mapping.annotations.QueryParameters
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbAccessKey
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbRole
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbUser

/**
 * UserRoleAccessor
 * Created by Ralf Ulrich on 22.01.16.
 */
@Accessor
interface UserRoleAccessor {

    @Query("SELECT * FROM LADON.USERS")
    @QueryParameters(consistency = "ONE")
    fun getAllUsers(): Result<DbUser>

    @Query("SELECT * FROM LADON.KEYS")
    @QueryParameters(consistency = "ONE")
    fun getAllKeys(): Result<DbAccessKey>

    @Query("SELECT * FROM LADON.ROLES WHERE ROLEID = :role")
    @QueryParameters(consistency = "ONE")
    fun getAllMembers(role: String): Result<DbRole>

    @Query("SELECT DISTINCT ROLEID FROM LADON.ROLES")
    @QueryParameters(consistency = "ONE")
    fun getAllRoles(): ResultSet

}