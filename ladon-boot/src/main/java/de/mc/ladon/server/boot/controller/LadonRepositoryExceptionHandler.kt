package de.mc.ladon.server.boot.controller

import de.mc.ladon.server.core.api.LadonRepositoryException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest


@ControllerAdvice
open class LadonRepositoryExceptionHandler {


    @ExceptionHandler(LadonRepositoryException::class)
    open fun handleConflict(req: HttpServletRequest, e: LadonRepositoryException): ResponseEntity<Any> {
        return ResponseEntity("{ \"message\" : \"${e.message}\" }", HttpStatus.valueOf(e.status))
    }

}
