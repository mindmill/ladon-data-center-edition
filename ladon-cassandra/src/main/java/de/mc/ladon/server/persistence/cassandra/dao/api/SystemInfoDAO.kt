package de.mc.ladon.server.persistence.cassandra.dao.api

import com.datastax.driver.core.Row

/**
 * SystemInfoDAO
 * Created by Ralf Ulrich on 25.04.15.
 */
interface SystemInfoDAO {

    fun getLocalInformation(): List<Row>

    fun getPeerInformation(): List<Row>

    fun getKeySpaceInformation(): List<String>

}