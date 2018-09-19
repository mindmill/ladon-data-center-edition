/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.impl

import com.datastax.driver.core.*
import de.mc.ladon.server.persistence.cassandra.dao.api.StatementCache
import de.mc.ladon.server.persistence.cassandra.database.SessionProvider
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * StatementCacheImpl
 * Created by Ralf Ulrich on 01.02.15.
 */
@Named open class StatementCacheImpl @Inject constructor(val session: SessionProvider) : StatementCache {

    val LOG = LoggerFactory.getLogger(javaClass)
    val statements: MutableMap<String, PreparedStatement> = HashMap()


    override fun <T> executePrepared(query: String, resultMapper: (ResultSet) -> T, vararg binds: Any): T {
        val result = executePrepared(query, *binds)
        return resultMapper.invoke(result)
    }

    override fun executePreparedAsync(query: String, vararg binds: Any): ResultSetFuture {
        return executeAsync(query, *binds)
    }

    fun executePrepared(query: String, vararg binds: Any): ResultSet {
        val pstmt = getStatement(session.get(), query).setConsistencyLevel(ConsistencyLevel.ONE)
        return session.get().execute(pstmt.bind(*binds))
    }

    override fun getPreparedStatement(query: String, vararg binds: Any): Statement {
        return getStatement(session.get(), query).bind(*binds).setConsistencyLevel(ConsistencyLevel.ONE)
    }

    fun executeAsync(query: String, vararg binds: Any): ResultSetFuture {
        val pstmt = getStatement(session.get(), query).setConsistencyLevel(ConsistencyLevel.ONE)
        return session.get().executeAsync(pstmt.bind(*binds))
    }

    private fun getStatement(session: Session, name: String): PreparedStatement {
        val stmt = statements.get(name)
        return if (stmt == null) {
            LOG.info("preparing statement : {$name}")
            val new = session.prepare(name)
            //new.enableTracing()
            synchronized(this) {
                statements.put(name, new)
            }
            new
        } else {
            //println("hit {$count}")
            //count++
            stmt
        }
    }


}