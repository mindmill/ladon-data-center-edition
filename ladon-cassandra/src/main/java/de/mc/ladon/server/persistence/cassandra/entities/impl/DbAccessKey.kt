package de.mc.ladon.server.persistence.cassandra.entities.impl

import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table

/**
 * @author Ralf Ulrich
 * 22.10.16
 */
@Table(keyspace = "LADON", name = "KEYS", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
data class DbAccessKey(
        @PartitionKey
        var accessKeyId: String? = null,
        var secretKey: String? = null,
        var userid: String? = null,
        var roles: Set<String>? = null
)