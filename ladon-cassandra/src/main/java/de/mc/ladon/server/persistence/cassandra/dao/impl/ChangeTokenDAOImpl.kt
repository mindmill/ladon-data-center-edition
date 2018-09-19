/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.impl

import com.datastax.driver.core.ResultSet
import de.mc.ladon.server.core.exceptions.LadonStorageException
import de.mc.ladon.server.core.persistence.dao.api.ChangeTokenDAO
import de.mc.ladon.server.core.persistence.entities.api.ChangeToken
import de.mc.ladon.server.core.request.LadonCallContext
import de.mc.ladon.server.persistence.cassandra.dao.api.StatementCache
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbChangeToken
import java.math.BigInteger
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * DAO for changetoken database objects
 * Created by Ralf Ulrich on 07.05.16.
 */
@Named
open class ChangeTokenDAOImpl @Inject constructor(val dbQuery: StatementCache) : ChangeTokenDAO {

    override fun getAllChangesSince(cc: LadonCallContext, repoId: String, token: String, maxItems: BigInteger?): List<ChangeToken> {
        val max = maxItems?.toInt() ?: 10000
        val uuid = try {
            UUID.fromString(token)
        } catch (e: IllegalArgumentException) {
            throw LadonStorageException("illegal change token ${token}")
        }
        return dbQuery.executePrepared("SELECT * FROM ladon.changetoken_view WHERE repoid = :repoid AND CHANGETOKEN >= :changetoken", { rs ->
            changeTokenMapper(rs).take(max)

        }, repoId, uuid)
    }


    override fun getLatestChangeToken(cc: LadonCallContext, repoId: String, maxItems: Long?): List<ChangeToken> {
        return dbQuery.executePrepared("SELECT * FROM ladon.changetoken_view WHERE REPOID = :repoid  ORDER BY CHANGETOKEN DESC LIMIT $maxItems", { rs ->
            changeTokenMapper(rs)
        }, repoId)
    }

    override fun getFirstChangeToken(cc: LadonCallContext, repoId: String): ChangeToken? {
        return dbQuery.executePrepared("SELECT * FROM ladon.changetoken_view WHERE REPOID = :repoid ORDER BY CHANGETOKEN ASC LIMIT 1", { rs ->
            changeTokenMapper(rs).firstOrNull()
        }, repoId)
    }

    private fun changeTokenMapper(rs: ResultSet) = rs.map {
        DbChangeToken(it.getString("versionseriesid"), it.getString("operation"), it.getString("repoid"),
                it.getUUID("changetoken"))
    }
}