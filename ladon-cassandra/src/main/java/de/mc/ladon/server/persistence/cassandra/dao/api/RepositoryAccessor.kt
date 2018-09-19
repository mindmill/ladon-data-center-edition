package de.mc.ladon.server.persistence.cassandra.dao.api

import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Query
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbRepository

/**
 * @author Ralf Ulrich
 * 06.11.16
 */
@Accessor
interface RepositoryAccessor {

    @Query("SELECT * FROM LADON.REPOSITORIES")
    fun listRepositories(): Result<DbRepository>
}