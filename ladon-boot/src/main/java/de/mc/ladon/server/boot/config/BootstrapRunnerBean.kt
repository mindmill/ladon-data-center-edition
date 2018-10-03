package de.mc.ladon.server.boot.config

import de.mc.ladon.server.core.api.bootstrap.BootstrapRunner
import de.mc.ladon.server.core.api.persistence.Database
import de.mc.ladon.server.core.api.persistence.DatabaseConfig
import de.mc.ladon.server.core.bootstrap.impl.CreateAdminUserTask
import de.mc.ladon.server.core.bootstrap.impl.CreateSystemRepoTask
import de.mc.ladon.server.core.bootstrap.impl.InitDatabaseTask
import de.mc.ladon.server.persistence.cassandra.bootstrap.PrepareDataDirTask
import de.mc.ladon.server.persistence.cassandra.bootstrap.StartCassandraTask
import org.springframework.beans.factory.SmartInitializingSingleton
import javax.inject.Inject
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * *         24.10.16
 */
@Named
open class BootstrapRunnerBean @Inject constructor(val runner: BootstrapRunner,
                                                   val database: Database,
                                                   val createAdminUserTask: CreateAdminUserTask,
                                                   val createSystemRepoTask: CreateSystemRepoTask,
                                                   val config: DatabaseConfig) : SmartInitializingSingleton {


    override fun afterSingletonsInstantiated() {
        runner.run(PrepareDataDirTask(config))
        runner.run(StartCassandraTask(database))
        runner.run(InitDatabaseTask(database))
        runner.run(createAdminUserTask)
        runner.run(createSystemRepoTask)

    }
}
