package de.mc.ladon.server.core.persistence.entities.api

/**
 * Database object for the binary content
 * Created by Ralf Ulrich on 05.05.16.
 */
interface Content {
    var repoId: String

    var streamId: String

    var count: Long?

    var chunkId: String
}
