package de.mc.ladon.server.core.api.executor

import java.util.concurrent.Future

/**
 * Simple Java Executor Service Wrapper
 * Created by Ralf Ulrich on 06.02.16.
 */
interface LadonExecutorService {


    fun execute(closure: () -> Unit): Unit

    fun <T> submit(closure: () -> T): Future<T>

    fun shutdown()

}