package de.mc.ladon.server.core.persistence.entities.api

import java.util.*

/**
 * Change Token database representation
 * Created by Ralf Ulrich on 05.05.16.
 */
interface ChangeToken {

    var versionseriesId: String?

    var operation: String?

    var repoId: String?

    var changeToken: UUID?


}
