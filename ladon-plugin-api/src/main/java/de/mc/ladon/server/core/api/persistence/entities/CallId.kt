/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.api.persistence.entities

import java.util.*

/**
 * CallId interface for a time based uuid identifying the current call and call time
 * the call id is also used as changetoken to identify changes in the database
 * Created by Ralf Ulrich on 27.07.16.
 */
interface CallId {
    fun id(): UUID
}