/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.tasks

import com.datastax.driver.core.utils.UUIDs
import de.mc.ladon.server.core.bootstrap.api.BootstrapTask
import de.mc.ladon.server.core.persistence.entities.api.ChangeType
import de.mc.ladon.server.persistence.cassandra.dao.api.ObjectDataAccessor
import de.mc.ladon.server.persistence.cassandra.database.MappingManagerProvider
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbObjectData
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Migrate the Schema to V2 if necessary
 */
class MigrateToV2Task(val mm: MappingManagerProvider) : BootstrapTask {

    val log = LoggerFactory.getLogger(javaClass)
    val mapper = mm.getMapper(DbObjectData::class.java)
    val accessor = mm.getAccessor((ObjectDataAccessor::class.java))

    override fun shouldRun(): Boolean {
        val noDeletedFound = !accessor.value.getAllObjects().asSequence().filter { it.operation == ChangeType.DELETE.name }.any()
        log.info("Migration Check : $noDeletedFound")
        return noDeletedFound
    }

    override fun run() {
        log.info("Start migration to version 2")
        accessor.value.getAllObjects().asSequence().filter { it.deleted != null }.forEach {
            log.info("found deleted meta : $it")
            val newChangeToken = UUIDs.startOf((it.deleted ?: Date()).time)
            val oldMeta = it.copy(deleted = null, deletedBy = null)
            val newMeta = it.copy(changeToken = newChangeToken, operation = ChangeType.DELETE.name)
            log.info("saving new meta : $newMeta")
            mapper.value.save(newMeta)
            log.info("updating old meta : $oldMeta")
            mapper.value.save(oldMeta)
        }
        log.info("end migration to version 2")

    }


    override fun isFatal(): Boolean {
        return false
    }
}