package de.mc.ladon.server.persistence.cassandra.entities.impl

import de.mc.ladon.server.core.persistence.entities.api.ChangeToken
import java.util.*

/**
 * Database changetoken with cassandra annotations
 * Created by Ralf Ulrich on 05.05.16.
 */
open class DbChangeToken(override var versionseriesId: String? = null,
                         override var operation: String? = null,
                         override var repoId: String? = null,
                         override var changeToken: UUID? = null) : ChangeToken