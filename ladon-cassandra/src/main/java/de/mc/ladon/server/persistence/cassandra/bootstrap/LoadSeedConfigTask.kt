package de.mc.ladon.server.persistence.cassandra.bootstrap

import de.mc.ladon.server.core.bootstrap.api.BootstrapTask
import de.mc.ladon.server.core.persistence.DatabaseConfig
import de.mc.ladon.server.persistence.cassandra.database.LadonSeedProvider
import java.net.InetAddress
import javax.inject.Inject

/**
 * @author Ralf Ulrich
 * 24.10.16
 */
//@Named
class LoadSeedConfigTask : BootstrapTask {

    @Inject
    lateinit var config: DatabaseConfig

    override fun shouldRun(): Boolean {
        return false
    }

    override fun run() {
        LadonSeedProvider.ladonSeeds = config.nodes!!.map { InetAddress.getByName(it) }
    }

    override fun isFatal(): Boolean {
        return true
    }
}