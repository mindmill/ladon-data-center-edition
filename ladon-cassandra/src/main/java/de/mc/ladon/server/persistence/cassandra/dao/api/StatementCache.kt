/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.api

import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.ResultSetFuture
import com.datastax.driver.core.Statement

/**
 * StatementCache
 * Created by Ralf Ulrich on 01.02.15.
 */
interface StatementCache {

    fun <T> executePrepared(query: String, resultMapper: (ResultSet) -> T, vararg binds: Any): T

    fun executePreparedAsync(query: String, vararg binds: Any): ResultSetFuture

    fun getPreparedStatement(query: String, vararg binds: Any): Statement
}