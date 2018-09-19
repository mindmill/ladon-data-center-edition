package de.mc.ladon.server.persistence.cassandra.entities.impl

import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import java.util.*

/**
 * Object data with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
@Table(keyspace = "LADON", name = "OBJECTS", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
data class DbObjectData(
        @PartitionKey
        var repoId: String? = null,
        @ClusteringColumn
        var versionseriesId: String? = null,
        @ClusteringColumn(1)
        var changeToken: UUID? = null,
        var operation: String? = null,
        var meta: MutableMap<String, String>? = null,
        var streamid: String? = null,
        var length: Long? = null,
        var md5: String? = null,
        var created: Date? = null,
        var createdBy: String? = null,
        var deleted: Date? = null,
        var deletedBy: String? = null
)

