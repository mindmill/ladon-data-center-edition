/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.boot.config

import com.datastax.driver.core.utils.UUIDs
import de.mc.ladon.server.core.persistence.entities.api.CallId
import de.mc.ladon.server.core.persistence.entities.api.User
import de.mc.ladon.server.core.persistence.entities.impl.LadonCallId
import de.mc.ladon.server.core.request.LadonCallContext

/**
 * @author Ralf Ulrich
 * 25.10.16.
 */
class WebCallContext(val webuser: User) : LadonCallContext {
    override fun getObjectId(): String? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setObjectId(objId: String) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPath(): String? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRepositoryId(): String? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val id = LadonCallId(UUIDs.timeBased())

    override fun getUser(): User {
        return webuser
    }

    override fun getCallId(): CallId {
        return id
    }
}