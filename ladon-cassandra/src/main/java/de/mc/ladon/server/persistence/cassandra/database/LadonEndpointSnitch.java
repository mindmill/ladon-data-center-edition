/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.database;

import org.apache.cassandra.locator.AbstractNetworkTopologySnitch;

import java.net.InetAddress;

/**
 * @author Ralf Ulrich
 *         on 24.09.16.
 */
public class LadonEndpointSnitch extends AbstractNetworkTopologySnitch {

    public static String rack;
    public static String datacenter;


    @Override
    public String getRack(InetAddress endpoint) {
        return rack;
    }

    @Override
    public String getDatacenter(InetAddress endpoint) {
        return datacenter;
    }
}
