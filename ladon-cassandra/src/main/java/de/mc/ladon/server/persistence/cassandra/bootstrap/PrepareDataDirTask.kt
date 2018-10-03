/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.bootstrap

import de.mc.ladon.server.core.api.bootstrap.BootstrapTask
import de.mc.ladon.server.core.api.persistence.DatabaseConfig
import de.mc.ladon.server.core.util.PathUtils.getLadonHome
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Creates the directories for the embedded cassandra instance.
 */
class PrepareDataDirTask(val config: DatabaseConfig) : BootstrapTask {
    private val LOG = LoggerFactory.getLogger(javaClass)

    val ladonHome = getLadonHome()
    val dataDir = ladonHome + File.separator + "cassandra"
    val configDir = ladonHome + File.separator + "conf"
    val cassandraConfig = configDir + File.separator + "cassandra.yaml"
    val topologyFile = configDir + File.separator + "ladon-config.properties"
    val rackDcProps = configDir + File.separator + "cassandra-rackdc.properties"
    val triggersDir = dataDir + File.separator + "triggers"

    override fun shouldRun() = true


    override fun run() {
        System.setProperty("cassandra.storagedir", dataDir)
        System.setProperty("cassandra.triggers_dir", triggersDir)
        System.setProperty("cassandra-rackdc.properties", Paths.get(rackDcProps).toUri().toASCIIString())
        System.setProperty("ladon.home", ladonHome)


        if (!Files.exists(Paths.get(configDir))) {
            initLadonData()
        }

        loadLadonConfig(config)
    }

    private fun loadLadonConfig(config: DatabaseConfig) {
        val ladonConfig = LadonConfig(Properties().apply { load(Paths.get(topologyFile).toFile().reader()) })
        config.nodes = ladonConfig.getSeeds()
        config.ownIp = ladonConfig.getSelfAddress()
        config.datacenter = ladonConfig.getSelfDatacenter()
        config.rack = ladonConfig.getSelfRack()
        config.replicationfactor = generateReplicationFactorString(ladonConfig)
    }

    private fun generateReplicationFactorString(config: LadonConfig): String {
        return config.nodes.groupBy { it.dc }
                .map { "'${it.key}' : ${if (it.value.size == 1) 1 else 2}" }
                .joinToString()

    }

    fun initLadonData() {
        val config = LadonConfigClient().getConfig()

        Files.createDirectories(Paths.get(triggersDir))
        LOG.info("Created Ladon data directory : $dataDir")
        Files.createDirectories(Paths.get(configDir))
        LOG.info("Created Ladon config directory : $configDir")


        writeCassandraYaml(config)
        LOG.info("Created Ladon config  : $cassandraConfig")
        writeRackDcFile(config)
        LOG.info("Created RackDc config  : $rackDcProps")
        writeLadonConfigFile(config)
        LOG.info("Created Ladon config  : $topologyFile")

    }

    private fun writeLadonConfigFile(config: LadonConfig) {
        var sw: FileWriter? = null
        try {
            sw = FileWriter(Paths.get(topologyFile).toFile())
            config.raw.store(sw, "Ladon Config")
        } catch (e: Exception) {
            LOG.error("error while initializing $topologyFile")
        } finally {
            sw?.close()
        }
    }

    private fun writeCassandraYaml(config: LadonConfig) {
        writeTemplateFile("cassandra.yaml", cassandraConfig, mapOf<String, Any>(
                "hostname" to config.getSelfAddress(),
                "seeds" to config.getSeeds().joinToString(",")))
    }

    private fun writeRackDcFile(config: LadonConfig) {
        writeTemplateFile("cassandra-rackdc.properties", rackDcProps, mapOf<String, Any>(
                "dc" to config.getSelfDatacenter(),
                "rack" to config.getSelfRack()))
    }

    private fun writeTemplateFile(source: String, dest: String, tCon: Map<String, Any>) {
        val context = VelocityContext()
        tCon.forEach { context.put(it.key, it.value) }
        var sw: FileWriter? = null
        try {
            val p = Properties()
            p.setProperty("resource.loader", "class")
            p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
            Velocity.init(p)
            val template = Velocity.getTemplate(source)
            sw = FileWriter(Paths.get(dest).toFile())
            template!!.merge(context, sw)
        } catch (e: Exception) {
            LOG.error("error while initializing $source")
        } finally {
            sw?.close()
        }
    }


    override fun isFatal(): Boolean {
        return true
    }
}