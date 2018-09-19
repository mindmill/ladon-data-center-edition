package de.mc.ladon.server.boot.config

import de.mc.ladon.server.persistence.cassandra.dao.api.StatementCache
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * 11.05.17
 */
@Named
open class CassandraHealthCheck @Inject constructor(val dbQuery: StatementCache) : AbstractHealthIndicator() {

    override fun doHealthCheck(builder: Health.Builder) {
        try {
            val version = dbQuery.executePrepared("SELECT release_version FROM system.local", { rs -> rs.one().getString(0) })
            builder.up().withDetail("version", version)
        } catch (ex: Exception) {
            builder.down(ex)
        }
    }
}