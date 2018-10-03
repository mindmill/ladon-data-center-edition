package de.mc.ladon.server.core.executor.impl

import de.mc.ladon.server.core.api.executor.LadonExecutorConfig
import de.mc.ladon.server.core.api.executor.LadonExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

/**
 * LadonExecutorServiceImpl
 * Created by Ralf Ulrich on 06.02.16.
 */
@Named
open class LadonExecutorServiceImpl @Inject constructor(val config: LadonExecutorConfig) : LadonExecutorService {

    private val es: ExecutorService


    init {
        es = Executors.newFixedThreadPool(config.getWebthreads())

    }


    override fun <T> submit(closure: () -> T): Future<T> {
        return es.submit(closure)
    }

    override fun execute(closure: () -> Unit) {
        es.execute(closure)
    }

    override fun shutdown() {
        es.shutdown()
        es.awaitTermination(5, TimeUnit.MINUTES)
    }

}