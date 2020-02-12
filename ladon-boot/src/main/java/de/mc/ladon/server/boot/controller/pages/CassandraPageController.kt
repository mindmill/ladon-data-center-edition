package de.mc.ladon.server.boot.controller.pages

import com.datastax.driver.core.Host
import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.core.api.bootstrap.BootstrapRunner
import de.mc.ladon.server.core.api.persistence.Database
import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager
import de.mc.ladon.server.core.bootstrap.impl.CreateAdminUserTask
import de.mc.ladon.server.persistence.cassandra.dao.api.SystemInfoDAO
import de.mc.ladon.server.persistence.cassandra.database.DatabaseImpl
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Controller for the cassandra info page
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class CassandraPageController : FrameController() {

    @Autowired
    lateinit var systemDao: SystemInfoDAO
    @Autowired
    lateinit var database: Database
    @Autowired
    lateinit var runner: BootstrapRunner
    @Autowired
    lateinit var mm: MappingManagerProvider
    @Autowired
    lateinit var userDetailsManager: LadonUserDetailsManager


    @RequestMapping("cassandra")
    fun cassandra(model: MutableMap<String, Any>, @RequestParam repoid: String): String {
        model.put("local", systemDao.getLocalInformation())
        model.put("peers", systemDao.getPeerInformation())
        model.put("keyspace", systemDao.getKeySpaceInformation())
        try {
            val hosts: Map<String,List<Host>> = (database as DatabaseImpl).clusterMetadata.allHosts.groupBy { it.datacenter }
            model.put("dcs", hosts.map { Datacenter(it.value,it.key) } )
            model.put("clustername", (database as DatabaseImpl).clusterMetadata.clusterName)
        } catch (e: IllegalStateException) {
            model.flashDanger(e.message ?: "ALL CASSANDRA HOSTS ARE DOWN")
        }
        return super.updateModel(model, "cassandra", repoid)
    }

//    @RequestMapping("cassandra/migrateschema")
//    fun migrateSchema(model: MutableMap<String, Any>): String {
//        runner.run(MigrateToV2Task(mm))
//        return "redirect:../overview"
//    }


    @RequestMapping("cassandra/init")
    fun initCassandra(): String {
        database.initSchema()
        if (!userDetailsManager.userExists("admin"))
            CreateAdminUserTask(userDetailsManager).run()
        return "redirect:/ladon/overview"
    }

    @RequestMapping("cassandra/replication")
    fun initCassandra(@RequestParam("factor") factor: Int): String {
        (database as DatabaseImpl).updateReplication(factor)
        return "redirect:/ladon/overview"
    }
}

data class Datacenter(val hosts: List<Host>, val name : String)
