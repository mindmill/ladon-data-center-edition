package de.mc.ladon.server.persistence.cassandra.bootstrap

import org.slf4j.LoggerFactory
import java.util.*


class LadonConfigClient {

    private val LOG = LoggerFactory.getLogger(javaClass)

    fun getConfig(): LadonConfig {
        return if (System.console() == null) {
            LadonConfig(Properties().apply {
                put("self", "127.0.0.1")
                put("127.0.0.1", "datacenter1:RC1")
            })
        } else {
            val hostName = System.console().readLine("Listen address (localhost) ").defaultIfEmpty("localhost")
            val datacenter = System.console().readLine("Datacenter (datacenter1) ").defaultIfEmpty("datacenter1")
            val rack = System.console().readLine("Rack (RC1) ").defaultIfEmpty("RC1")
            val nodes = System.console().readLine("Other Nodes, format: hostname:datacenter:rack,hostname2:datacenter2:rack2 ")
                    .split(",").filterNot { it.isEmpty() }.map { it.split(":") }
            LadonConfig(Properties().apply {
                put("self", hostName)
                put(hostName, "$datacenter:$rack")
                nodes.forEach { put(it[0], "${it[1]}:${it[2]}") }
            })
        }
    }
}

fun String.defaultIfEmpty(default: String) = if (isNullOrEmpty()) default else this
data class LadonConfig(val raw: Properties) {

    val nodes = getSeeds().map { it to raw.getProperty(it) }
            .map { LadonNode(it.first, it.second.substringBefore(":"), it.second.substringAfter(":")) }

    fun getSelfAddress() = raw.getProperty("self")

    fun getSeeds() = (raw.stringPropertyNames() - "self").toList()

    fun getSelfDatacenter(): String {
        val dcRack = raw.getProperty(getSelfAddress())
        return dcRack.substringBefore(":")
    }

    fun getSelfRack(): String {
        val dcRack = raw.getProperty(getSelfAddress())
        return dcRack.substringAfter(":")
    }
}

data class LadonNode(val address: String, val dc: String, val rack: String)
