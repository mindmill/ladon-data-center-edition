package de.mc.ladon.cmis

import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

@Named
class LadonServicesHolder @Inject constructor(applicationContext: ApplicationContext) {
    init {
        ctx = applicationContext
    }

    companion object {

        lateinit var ctx: BeanFactory
        fun userDetailsManager() = ctx.getBean(LadonUserDetailsManager::class.java)
        fun <T> getService(clazz: Class<T>) = ctx.getBean(clazz)

    }


}
