package de.mc.ladon.cmis

import de.mc.ladon.server.core.api.persistence.entities.User
import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager
import org.springframework.beans.factory.BeanFactory
import org.springframework.core.ResolvableType

class TestContext(vararg beans: Pair<Class<*>, Any>) : BeanFactory {
    val ctxBeans = hashMapOf<Class<*>, Any>()

    init {
        beans.forEach { ctxBeans[it.first] = it.second }
    }

    override fun isSingleton(name: String?) = true

    override fun containsBean(name: String?) = true

    override fun getBean(name: String?): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> getBean(name: String?, requiredType: Class<T>?): T {
        TODO("not implemented")
    }

    override fun <T : Any?> getBean(requiredType: Class<T>?): T {
        return ctxBeans[requiredType!!] as? T ?: throw IllegalStateException()
    }

    override fun getBean(name: String?, vararg args: Any?): Any {
        TODO("not implemented")
    }

    override fun <T : Any?> getBean(requiredType: Class<T>?, vararg args: Any?): T {
        TODO("not implemented")
    }

    override fun isPrototype(name: String?) = false

    override fun getType(name: String?): Class<*> {
        TODO("not implemented")
    }

    override fun isTypeMatch(name: String?, typeToMatch: ResolvableType?): Boolean {
        TODO("not implemented")
    }

    override fun isTypeMatch(name: String?, typeToMatch: Class<*>?): Boolean {
        TODO("not implemented")
    }

    override fun getAliases(name: String?): Array<String> {
        TODO("not implemented")
    }
}

class TestUserDetailsManager(vararg val users: User) : LadonUserDetailsManager {
    override fun loadUserByUsername(name: String) = users.find { it.name == name }!!
    override fun userExists(name: String) = users.any { it.name == name }
    override fun updateUser(user: User) = throw IllegalStateException("cant update test users")
    override fun createUser(user: User) = throw IllegalStateException("cant create test users")
    override fun deleteUser(name: String) = throw IllegalStateException("cant delete test users")
    override fun changePassword(name: String, oldPassword: String, newPassword: String) = throw IllegalStateException("cant update test users password")
}

