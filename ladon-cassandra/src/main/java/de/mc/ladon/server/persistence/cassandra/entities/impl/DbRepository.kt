package de.mc.ladon.server.persistence.cassandra.entities.impl

import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import de.mc.ladon.server.core.api.persistence.entities.Repository
import java.util.*

/**
 * Repository database object with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
@Table(keyspace = "LADON", name = "REPOSITORIES", readConsistency = "ONE", writeConsistency = "LOCAL_QUORUM")
data class DbRepository(
        @PartitionKey
        override var repoId: String? = null,
        override var createdby: String? = null,
        override var creationdate: Date? = null,
        override var acl: String? = null,
        override var policy: String? = null,
        override var versioned: Boolean? = null,
        override var region: String? = null
) : Repository
