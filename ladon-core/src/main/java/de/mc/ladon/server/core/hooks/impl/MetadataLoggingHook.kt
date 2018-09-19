/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.hooks.impl

import de.mc.ladon.server.core.hooks.api.MetadataChangeHook
import de.mc.ladon.server.core.persistence.entities.api.Metadata
import de.mc.ladon.server.core.persistence.entities.impl.ResourceKey
import org.slf4j.LoggerFactory

/**
 * LoggingObjectDataHook
 * Created by Ralf Ulrich on 07.07.15.
 */
//@Named
open class MetadataLoggingHook : MetadataChangeHook {


    private val LOG = LoggerFactory.getLogger(javaClass)

    override fun onBeforeCreateObject(key: ResourceKey, before: Metadata) {
        LOG.info("Log before create $before ")
    }


    override fun onAfterCreateObject(key: ResourceKey, after: Metadata) {
        LOG.info("Log after create $after ")
    }


    override fun onAfterUpdateObject(key: ResourceKey, before: Metadata, after: Metadata) {
        LOG.info("Log after update : before $before , after $after")
    }


    override fun onAfterDeleteObject(key: ResourceKey, after: Metadata) {
        LOG.info("Log after delete : after $after")
    }

    override fun priority(): Int {
        return 0
    }
}