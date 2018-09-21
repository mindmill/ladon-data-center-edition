package de.mc.ladon.server.persistence.cassandra.bootstrap

import de.mc.ladon.server.core.util.getLogger
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.URL
import java.util.*


class LadonConfigClient {

    private val LOG = getLogger()
    private val localSetup = System.getProperty("localsetup") != null

    fun getConfig(): LadonConfig {
        val buf = "LADON-DE-1".toByteArray()

        val socket = MulticastSocket(4446)
        val group = InetAddress.getByName("230.0.0.0")
        socket.joinGroup(group)
        val packet = DatagramPacket(buf, buf.size, group, 4446)
        LOG.info("Sending Config Server lookup packet...")
        socket.send(packet)
        LOG.info("Waiting for Config Server to respond...")
        var port = 8888
        while (!localSetup) {
            socket.receive(packet)
            val received = String(packet.data, 0, packet.length)
            try {
                port = received.toInt()
                break
            } catch (e: NumberFormatException) {
                LOG.info("skip multicast message : $received")
            }
        }
        val configServerAddress = if (localSetup) {
            return LadonConfig(Properties().apply {
                put("self", "127.0.0.1")
                put("127.0.0.1", "DC1:RC1")
            })
        } else {
            packet.address.hostAddress
        }
        LOG.info("Located Config Server at $configServerAddress:$port")
        try {
            val configText = URL("http://$configServerAddress:$port/").readText()
            LOG.info("Read config success")
            return LadonConfig(Properties().apply { loadFromXML(ByteArrayInputStream(configText.toByteArray())) })
        } catch (e: IOException) {
            if (e.message?.contains("409") == true) {
                LOG.error("Configuration not valid, the nodes address could not be found at the config server")
            } else {
                LOG.error("Configuration could not be found, make sure the config server is running and no firewall active on port $port")
            }
            throw e
        }


    }

}

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