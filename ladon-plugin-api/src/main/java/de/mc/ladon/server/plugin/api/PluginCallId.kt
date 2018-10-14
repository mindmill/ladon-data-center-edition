/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.plugin.api

import de.mc.ladon.server.core.api.persistence.entities.CallId
import java.util.*

/**
 * LadonCallId
 * Created by Ralf Ulrich on 27.07.16.
 */
class PluginCallId(val id: UUID) : CallId {
    override fun id() = id
}