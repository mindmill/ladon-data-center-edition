/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.bootstrap

import de.mc.ladon.server.core.api.bootstrap.BootstrapTask
import de.mc.ladon.server.core.api.persistence.DatabaseConfig
import de.mc.ladon.server.persistence.cassandra.database.LadonEndpointSnitch
import javax.inject.Inject

/**
 * Configures the snitch. This class transfers the DatabaseConfig into the EndpointSnitch
 */

class LoadSnitchConfigTask : BootstrapTask {

    @Inject
    lateinit var config: DatabaseConfig

    override fun shouldRun(): Boolean {
        return false
    }

    override fun run() {
        LadonEndpointSnitch.datacenter = config.datacenter
        LadonEndpointSnitch.rack = config.rack
    }


    override fun isFatal(): Boolean {
        return true
    }
}