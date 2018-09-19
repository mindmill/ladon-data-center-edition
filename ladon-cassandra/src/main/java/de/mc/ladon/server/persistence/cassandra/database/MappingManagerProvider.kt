package de.mc.ladon.server.persistence.cassandra.database

import com.datastax.driver.mapping.Mapper
import com.datastax.driver.mapping.MappingManager
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by ralf on 21.08.16.
 */
@Named
class MappingManagerProvider @Inject constructor(val sessionProvider: SessionProvider)  {

    fun <T> getMapper(type : Class<T>): Lazy<Mapper<T>> {
        return lazy { MappingManager(sessionProvider.get()).mapper(type) }
    }


    fun <T> getAccessor(type : Class<T>): Lazy<T> {
        return lazy { MappingManager(sessionProvider.get()).createAccessor(type) }
    }
}