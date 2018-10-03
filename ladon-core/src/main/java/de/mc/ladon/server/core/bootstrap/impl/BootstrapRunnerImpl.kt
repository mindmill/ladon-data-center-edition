package de.mc.ladon.server.core.bootstrap.impl

import de.mc.ladon.server.core.api.bootstrap.BootstrapRunner
import de.mc.ladon.server.core.api.bootstrap.BootstrapTask
import org.slf4j.LoggerFactory
import javax.inject.Named

/**
 * Simple bootstrap tool
 * Created by Ralf Ulrich on 26.04.15.
 */
@Named class BootstrapRunnerImpl : BootstrapRunner {
    private val LOG = LoggerFactory.getLogger(BootstrapRunnerImpl::class.java)


    override fun run(t: BootstrapTask) {
        val taskName = t.javaClass.simpleName
        LOG.info("RUN BOOTSTRAP TASK:  " + taskName)

        try {
            if (!t.shouldRun()) LOG.info("bootstrap check : skipping {} ", taskName)
            else {
                LOG.info("bootstrap start : {}", taskName)
                try {
                    t.run()
                    LOG.info("bootstrap finished : {}", taskName)
                } catch (e: Exception) {
                    if (t.isFatal()) throw e else
                        LOG.error("run {} failed", taskName, e)
                }
            }
        } catch (e: Exception) {
            if (t.isFatal()) throw e else
                LOG.error("check {} failed", taskName, e)
        }
    }

}

