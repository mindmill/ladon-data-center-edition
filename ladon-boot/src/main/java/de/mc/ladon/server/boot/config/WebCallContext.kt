/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot.config

import com.datastax.driver.core.utils.UUIDs
import de.mc.ladon.server.core.api.persistence.entities.CallId
import de.mc.ladon.server.core.api.persistence.entities.User
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.persistence.entities.impl.LadonCallId

/**
 * @author Ralf Ulrich
 * 25.10.16.
 */
class WebCallContext(val webuser: User) : LadonCallContext {
    override fun getObjectId(): String? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setObjectId(objId: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPath(): String? {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRepositoryId(): String? {
        throw UnsupportedOperationException("not implemented")
    }

    val id = LadonCallId(UUIDs.timeBased())

    override fun getUser(): User {
        return webuser
    }

    override fun getCallId(): CallId {
        return id
    }
}