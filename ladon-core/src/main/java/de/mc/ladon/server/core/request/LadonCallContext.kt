package de.mc.ladon.server.core.request

import de.mc.ladon.server.core.persistence.entities.api.CallId
import de.mc.ladon.server.core.persistence.entities.api.User

/**
 * LadonCallContext
 * Created by Ralf Ulrich on 07.02.16.
 */
interface LadonCallContext {

    fun getUser(): User

    fun getObjectId(): String?

    fun setObjectId(objId: String)

    fun getPath(): String?

    fun getRepositoryId(): String?

    fun getCallId(): CallId

}