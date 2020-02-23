package de.mc.ladon.server.boot.controller.pages

import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.boot.controller.humanReadable
import de.mc.ladon.server.core.util.PathUtils.getLadonHome
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.lang.management.ManagementFactory
import java.nio.file.Paths
import java.util.*

/**
 * SystemPageController
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class SystemPageController : FrameController() {


    @RequestMapping("system")
    fun system(model: MutableMap<String, Any>, @RequestParam repoid: String): String {
        val runtime = Runtime.getRuntime()
        val free = runtime.freeMemory()
        val max = runtime.maxMemory()
        val total = runtime.totalMemory()
        model["rtFree"] = free
        model["rtFreePercent"] = Math.round(free * 100.0 / max)
        model["rtFreeRead"] = free.humanReadable()
        model["rtMax"] = max
        model["rtMaxRead"] = max.humanReadable()
        model["rtTotal"] = total
        model["rtTotalRead"] = total.humanReadable()
        model["rtTotalPercent"] = Math.round(total * 100.0 / max)

        model["rtAvailable"] = (max - total)
        model["rtAvailablePercent"] = 100 - Math.round(free * 100.0 / max) - Math.round(total * 100.0 / max)
        model["rtAvailableRead"] = (max - total).humanReadable()

        model["time"] = Date().time

        model["rtProcessors"] = runtime.availableProcessors()
        model["fsRoots"] = arrayOf( Paths.get(getLadonHome()).toFile())

        model["osName"] = System.getProperty("os.name")
        model["osVersion"] = System.getProperty("os.version")
        model["osArch"] = System.getProperty("os.arch")

        //model.put("threads" , Thread.getAllStackTraces().keySet())
        //model.put("threads" , ManagementFactory.getThreadMXBean())
        return super.updateModel(model, "system", repoid)
    }

    @RequestMapping("cpuload", produces = arrayOf("application/x-javascript"))
    fun cpuLoad(model: MutableMap<String, Any>): String {
        val runtime = Runtime.getRuntime()
        val free = runtime.freeMemory()
        val max = runtime.maxMemory()
        val total = runtime.totalMemory()
        model["rtFree"] = free
        model["rtFreePercent"] = Math.round(free * 100.0 / max)
        model["rtFreeRead"] = free.humanReadable()
        model["rtMax"] = max
        model["rtMaxRead"] = max.humanReadable()
        model["rtTotal"] = total
        model["rtTotalRead"] = total.humanReadable()
        model["rtTotalPercent"] = Math.round(total * 100.0 / max)

        model["rtAvailable"] = (max - total)
        model["rtAvailablePercent"] = 100 - Math.round(free * 100.0 / max) - Math.round(total * 100.0 / max)
        model["rtAvailableRead"] = (max - total).humanReadable()

        model["rtProcessors"] = runtime.availableProcessors()
        model["fsRoots"] = arrayOf( Paths.get(getLadonHome()).toFile())
        model["time"] = Date().time

        try {
            javaClass.classLoader.loadClass("com.sun.management.OperatingSystemMXBean")
            val osBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean::class.java)
            model["jvmLoad"] = osBean.processCpuLoad * 100
            model["systemLoad"] = osBean.systemCpuLoad * 100
        } catch (e: Exception) {

            val osBean = ManagementFactory.getPlatformMXBean(java.lang.management.OperatingSystemMXBean::class.java)
            model["jvmLoad"] = 0
            model["systemLoad"] = osBean.systemLoadAverage * 100

        }
        return "cpuload"
    }


}
