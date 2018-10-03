package de.mc.ladon.server.boot.controller.pages

import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.boot.controller.humanReadable
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.File
import java.lang.management.ManagementFactory
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
        model.put("rtFree", free)
        model.put("rtFreePercent", Math.round(free * 100.0 / max))
        model.put("rtFreeRead", free.humanReadable())
        model.put("rtMax", max)
        model.put("rtMaxRead", max.humanReadable())
        model.put("rtTotal", total)
        model.put("rtTotalRead", total.humanReadable())
        model.put("rtTotalPercent", Math.round(total * 100.0 / max))

        model.put("rtAvailable", (max - total))
        model.put("rtAvailablePercent", 100 - Math.round(free * 100.0 / max) - Math.round(total * 100.0 / max))
        model.put("rtAvailableRead", (max - total).humanReadable())

        model.put("time", Date().time)

        model.put("rtProcessors", runtime.availableProcessors())
        model.put("fsRoots", File.listRoots())

        model.put("osName", System.getProperty("os.name"))
        model.put("osVersion", System.getProperty("os.version"))
        model.put("osArch", System.getProperty("os.arch"))

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
        model.put("rtFree", free)
        model.put("rtFreePercent", Math.round(free * 100.0 / max))
        model.put("rtFreeRead", free.humanReadable())
        model.put("rtMax", max)
        model.put("rtMaxRead", max.humanReadable())
        model.put("rtTotal", total)
        model.put("rtTotalRead", total.humanReadable())
        model.put("rtTotalPercent", Math.round(total * 100.0 / max))

        model.put("rtAvailable", (max - total))
        model.put("rtAvailablePercent", 100 - Math.round(free * 100.0 / max) - Math.round(total * 100.0 / max))
        model.put("rtAvailableRead", (max - total).humanReadable())

        model.put("rtProcessors", runtime.availableProcessors())
        model.put("fsRoots", File.listRoots())
        model.put("time", Date().time)

        try {
            javaClass.classLoader.loadClass("com.sun.management.OperatingSystemMXBean")
            val osBean = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean::class.java)
            model.put("jvmLoad", osBean.processCpuLoad * 100)
            model.put("systemLoad", osBean.systemCpuLoad * 100)
        } catch (e: Exception) {

            val osBean = ManagementFactory.getPlatformMXBean(java.lang.management.OperatingSystemMXBean::class.java)
            model.put("jvmLoad", 0)
            model.put("systemLoad", osBean.systemLoadAverage * 100)

        }
        return "cpuload"
    }


}
