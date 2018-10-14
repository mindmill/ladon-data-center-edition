package de.mc.ladon.server.plugin.api

import de.mc.ladon.server.core.api.persistence.entities.CallId
import de.mc.ladon.server.core.api.persistence.entities.User
import de.mc.ladon.server.core.api.request.LadonCallContext
import java.util.*

/**
 * SystemCallContext
 * Created by Ralf Ulrich on 18.08.16.
 */
class PluginCallContext : LadonCallContext {


    override fun getUser(): User {
        return PluginUser("admin")
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
        return PluginCallId(UUID.randomUUID())
    }
}