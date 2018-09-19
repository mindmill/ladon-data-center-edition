package de.mc.ladon.server.core.bootstrap.api

/**
 * Interface for bootstrap tasks
 * Created by Ralf Ulrich on 25.04.15.
 */
interface BootstrapTask {

    /**
     *  check whether the task should run or not
     */
    fun shouldRun(): Boolean

    /**
     * do the actual work
     */
    fun run()

    /**
     * Fatal means that the startup should stop if the task execution fails
     */
    fun isFatal(): Boolean
}