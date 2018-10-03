/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.s3

import com.datastax.driver.core.utils.UUIDs
import de.mc.ladon.s3server.entities.api.S3CallContext
import de.mc.ladon.server.core.api.persistence.entities.CallId
import de.mc.ladon.server.core.api.persistence.entities.User
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.persistence.entities.impl.LadonCallId
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import java.util.*


/**
 * @author Ralf Ulrich
 */
data class LadonS3CallContext(val s3CallContext: S3CallContext) : LadonCallContext {

    val callId: UUID = UUIDs.timeBased()

    override fun getUser(): User {
        return LadonUser(s3CallContext.user.userName, null, true, s3CallContext.user.roles)
    }

    override fun getObjectId(): String? {
        return null
    }

    override fun setObjectId(objId: String) {
    }

    override fun getPath(): String? {
        return null
    }

    override fun getRepositoryId(): String? {
        return null
    }

    override fun getCallId(): CallId {
        return LadonCallId(callId)
    }
}