/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.bootstrap.impl

import de.mc.ladon.server.core.api.bootstrap.BootstrapTask
import de.mc.ladon.server.core.api.persistence.Database

/**
 * CreateSystemRepositoryTask
 * Created by ralf on 26.04.15.
 */
open class InitDatabaseTask constructor(val database: Database) : BootstrapTask {


    override fun shouldRun(): Boolean {
        return true
    }

    override fun run() {
        database.init()
        database.initSchema()
    }

    override fun isFatal(): Boolean {
        return true
    }
}