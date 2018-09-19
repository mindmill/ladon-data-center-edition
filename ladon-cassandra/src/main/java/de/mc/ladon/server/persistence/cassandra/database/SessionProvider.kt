package de.mc.ladon.server.persistence.cassandra.database

import com.datastax.driver.core.Session
import de.mc.ladon.server.core.persistence.Database
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * Created by ralf on 21.08.16.
 */
@Named
class SessionProvider @Inject constructor(val database: Database) : Provider<Session> {

    override fun get(): Session {
        return (database as DatabaseImpl).session
    }
}