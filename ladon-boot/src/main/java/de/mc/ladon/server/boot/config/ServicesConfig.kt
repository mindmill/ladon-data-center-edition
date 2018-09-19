package de.mc.ladon.server.boot.config

import de.mc.ladon.server.persistence.cassandra.encryption.api.Encryptor
import de.mc.ladon.server.persistence.cassandra.encryption.impl.LadonEncryptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * ServiceConfig
 * Created by Ralf Ulrich on 22.11.15.
 */
@Configuration
open class ServicesConfig {


    @Bean
    open fun encryptor(config : DatabaseConfigImpl): Encryptor {
        return LadonEncryptor(config.password!!)
    }

}
