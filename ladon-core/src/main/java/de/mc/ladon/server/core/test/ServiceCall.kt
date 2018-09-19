/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.test

import de.mc.ladon.server.core.persistence.entities.api.CallId
import de.mc.ladon.server.core.persistence.entities.api.Metadata

/**
 * ServiceCall
 * Created by Ralf Ulrich on 18.08.16.
 */
class ServiceCall(val callId: CallId, val op: Operation, val input: Metadata, val output: Metadata, val result: Result)

enum class Operation {
    CREATE_OBJECT, UPDATE_OBJECT, DELETE_OBJECT
}

enum class Result {
    OK, NOK, EXCEPTION
}