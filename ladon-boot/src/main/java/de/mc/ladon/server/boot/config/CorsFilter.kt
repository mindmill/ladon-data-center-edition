package de.mc.ladon.server.boot.config

import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CorsFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS" == request.method) {
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE")
            response.addHeader("Access-Control-Allow-Credentials", "true")
            // response.addHeader("Access-Control-Allow-Headers", "Authorization")
            response.addHeader("Access-Control-Allow-Headers", "*")
            response.addHeader("Access-Control-Max-Age", "1")
        }
        filterChain.doFilter(request, response)
    }

}