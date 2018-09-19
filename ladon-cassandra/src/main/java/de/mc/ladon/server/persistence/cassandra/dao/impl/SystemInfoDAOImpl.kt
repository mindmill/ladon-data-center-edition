package de.mc.ladon.server.persistence.cassandra.dao.impl

import com.datastax.driver.core.Row
import de.mc.ladon.server.persistence.cassandra.dao.api.StatementCache
import de.mc.ladon.server.persistence.cassandra.dao.api.SystemInfoDAO
import javax.inject.Inject
import javax.inject.Named

/**
 * SystemInfoDAOImpl
 * Created by Ralf Ulrich on 25.04.15.
 */
@Named open class SystemInfoDAOImpl @Inject constructor(val dbQuery: StatementCache) : SystemInfoDAO {

    override fun getLocalInformation(): List<Row> {
        val rows = dbQuery.executePrepared("SELECT cluster_name, data_center, rack ,host_id,  release_version, schema_version FROM SYSTEM.LOCAL", { rs -> rs.all() })
        return rows
    }

    override fun getPeerInformation(): List<Row> {
        return dbQuery.executePrepared("SELECT data_center, rack ,host_id, preferred_ip, rpc_address,  release_version, schema_version FROM SYSTEM.PEERS", { rs -> rs.all() })
    }

    override fun getKeySpaceInformation(): List<String> {
        return dbQuery.executePrepared("SELECT replication FROM system_schema.keyspaces where keyspace_name = 'ladon'", { rs -> rs.all()
                .map { it.getMap(0,String::class.java, String::class.java).get("replication_factor")?:"" } })
    }
}