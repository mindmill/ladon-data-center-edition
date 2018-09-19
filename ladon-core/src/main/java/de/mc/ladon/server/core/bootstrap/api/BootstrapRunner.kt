package de.mc.ladon.server.core.bootstrap.api

/**
 * Simple interface for bootstrap tasks
 * Created by Ralf Ulrich on 26.04.15.
 */
interface BootstrapRunner {

     fun run(task: BootstrapTask)
}