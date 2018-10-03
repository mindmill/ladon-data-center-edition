package de.mc.ladon.server.core.request.impl

import de.mc.ladon.server.core.api.persistence.entities.CallId
import de.mc.ladon.server.core.api.persistence.entities.User
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.persistence.entities.impl.LadonCallId
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import java.util.*

/**
 * SystemCallContext
 * Created by Ralf Ulrich on 18.08.16.
 */
open class AnonymousCallContext : LadonCallContext {


    override fun getUser(): User {
        return LadonUser("anonymous")
    }

    override fun getObjectId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun setObjectId(objId: String) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getPath(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getRepositoryId(): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun getCallId(): CallId {
        // TODO this wont work for cassandra timeuuid
        return LadonCallId(UUID.randomUUID())
    }
}