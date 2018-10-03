package de.mc.ladon.server.core.api.persistence.entities

import java.util.*

/**
 * Repository
 * Created by Ralf Ulrich on 05.05.16.
 */
interface Repository {
    var repoId: String?
    var policy: String?
    var versioned: Boolean?
    var region : String?
    var createdby: String?
    var creationdate: Date?
    var acl: String?

}
