/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.api.tasks

import de.mc.ladon.server.core.api.request.LadonCallContext
import java.util.concurrent.Future

/**
 *
 * Created by ralfulrich on 30.04.15.
 */
interface RepositoryTaskRunner {

    fun <T> runTask(task: RepositoryTask<T>): Future<T>

    fun <T> runTask(callContext: LadonCallContext, task: RepositoryTask<T>): Future<T>

    fun shutdown()
}
