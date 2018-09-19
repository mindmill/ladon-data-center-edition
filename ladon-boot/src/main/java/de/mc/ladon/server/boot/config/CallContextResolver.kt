package de.mc.ladon.server.boot.config


import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import de.mc.ladon.server.core.request.LadonCallContext
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.inject.Named

/**
 * Resolver for the [LadonCallContext] object
 * Created by Ralf Ulrich on 14.02.16.
 */
@Named
class CallContextResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(methodParameter: MethodParameter): Boolean {
        return methodParameter.parameterType == LadonCallContext::class.java
    }

    @Throws(Exception::class)
    override fun resolveArgument(methodParameter: MethodParameter, modelAndViewContainer: ModelAndViewContainer, nativeWebRequest: NativeWebRequest, webDataBinderFactory: WebDataBinderFactory): Any {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as UserDetails?
        val user = if (userDetails != null) {
            LadonUser(userDetails.username)
        } else {
            LadonUser("anonymous")
        }
        return WebCallContext(user)
    }
}
