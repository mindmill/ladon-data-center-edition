/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.hooks.api

import de.mc.ladon.server.core.persistence.entities.api.Metadata
import de.mc.ladon.server.core.persistence.entities.impl.ResourceKey

/**
 * Simple interface to add features in a common way
 * Created by ralf on 07.07.15.
 */
interface MetadataChangeHook {

    fun onBeforeCreateObject(key: ResourceKey, before: Metadata)

    fun onAfterCreateObject(key: ResourceKey, after: Metadata)

    fun onAfterUpdateObject(key: ResourceKey, before: Metadata, after: Metadata)

    fun onAfterDeleteObject(key: ResourceKey, after: Metadata)

    /**
     * Number for order of execution. Int.MAX_VALUE means run first, 0 run last
     */
    fun priority(): Int
}