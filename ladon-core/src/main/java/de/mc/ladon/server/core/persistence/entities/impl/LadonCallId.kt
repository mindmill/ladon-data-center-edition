/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.entities.impl

import de.mc.ladon.server.core.api.persistence.entities.CallId
import java.util.*

/**
 * LadonCallId
 * Created by Ralf Ulrich on 27.07.16.
 */
class LadonCallId(val id: UUID) : CallId {
    override fun id() = id
}