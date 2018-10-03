package de.mc.ladon.server.core.bootstrap.impl

import de.mc.ladon.server.core.api.bootstrap.BootstrapTask
import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import javax.inject.Inject
import javax.inject.Named

/**
 * CreateSystemRepositoryTask
 * Created by ralf on 26.04.15.
 */
@Named open class CreateAdminUserTask @Inject constructor(val userDetailsManager: LadonUserDetailsManager) : BootstrapTask {


    override fun shouldRun(): Boolean {
        return !userDetailsManager.userExists("admin")
    }

    override fun run() {
        userDetailsManager.createUser(LadonUser("admin", "\$2a\$08\$ATrPV4GkDaNspmqT8Cog7O7ZiVJWVlxczHWaviFERJwa.ZzFAIsxe", true, setOf("admin", "user")))
    }

    override fun isFatal(): Boolean {
        return true
    }
}