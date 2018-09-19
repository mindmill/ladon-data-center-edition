/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.tasks.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import de.mc.ladon.server.core.request.LadonCallContext
import de.mc.ladon.server.core.request.SystemCallContext
import de.mc.ladon.server.core.tasks.api.RepositoryTask
import de.mc.ladon.server.core.tasks.api.RepositoryTaskRunner
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.inject.Named

/**

 * Created by ralf on 26.04.15.
 */
@Named
open class RepositoryTaskRunnerImpl : RepositoryTaskRunner {

    //private val LOG = LoggerFactory.getLogger(RepositoryTaskRunnerImpl::class.java)

    private val es = Executors.newFixedThreadPool(4, ThreadFactoryBuilder().setDaemon(true).build())


    override fun <T> runTask(task: RepositoryTask<T>): Future<T> {
        return es.submit(Callable { task.run(SystemCallContext()) })
    }

    override fun <T> runTask(callContext: LadonCallContext, task: RepositoryTask<T>): Future<T> {
        return es.submit(Callable { task.run(callContext) })
    }

    override fun shutdown() {
        es.shutdown()
        es.awaitTermination(10, TimeUnit.SECONDS)
    }
}
