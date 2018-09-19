/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.database

import org.apache.cassandra.locator.SeedProvider
import java.net.InetAddress

/**
 * @author Ralf Ulrich
 * on 23.09.16.
 */
open class LadonSeedProvider(args: Map<String, String>?) : SeedProvider {


    override fun getSeeds(): List<InetAddress> {
        return ladonSeeds.toMutableSet().plus(InetAddress.getByName("127.0.0.1")).toList()

    }

    companion object {
        @JvmStatic
        var ladonSeeds: List<InetAddress> = listOf()
    }
}