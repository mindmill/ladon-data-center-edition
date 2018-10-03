package de.mc.ladon.server.boot.config

import de.mc.ladon.server.core.api.persistence.DatabaseConfig
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration for the database
 * Created by Ralf Ulrich on 22.11.15.
 */
@ConfigurationProperties(prefix = "ladon.db")
open class DatabaseConfigImpl(
        override var datacenter: String? = null,
        override var rack: String? = null,
        override var replicationfactor: String? = null,
        override var user: String? = null,
        override var password: String? = null,
        override var ownIp: String? = null,
        override var port: Int? = null,
        override var nodes: List<String>? = null) : DatabaseConfig
