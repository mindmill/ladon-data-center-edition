package de.mc.ladon.server.plugin.runtime

import de.mc.ladon.server.core.util.getLadonHome
import de.mc.ladon.server.core.util.getLogger
import de.mc.ladon.server.core.util.getSystemDir
import org.apache.felix.framework.Felix
import org.apache.felix.framework.cache.BundleCache
import org.apache.felix.framework.util.FelixConstants
import org.osgi.framework.Bundle
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.inject.Named


class LadonRuntimeActivator : BundleActivator {
    val log = getLogger()
    private var context: BundleContext? = null

    fun getBundles(): Array<out Bundle> = context?.bundles.orEmpty()


    override fun start(context: BundleContext) {
        log.info("start host ativator")
        this.context = context

        context.addBundleListener {
            log.info(it.toString())

        }
    }

    override fun stop(context: BundleContext) {
        log.info("stop host activator")
        this.context = null
    }
}

@Named
class LadonPluginRuntime {

    private val activator: LadonRuntimeActivator
    private var felix: Felix

    init {
        Files.createDirectories(Paths.get(getSystemDir()))
        Runtime.getRuntime().addShutdownHook(Thread { shutdownApplication() })
        val config = HashMap<String, Any>()
        activator = LadonRuntimeActivator()
        val list = ArrayList<BundleActivator>()
        list.add(activator)
        list.add(org.slf4j.osgi.logservice.impl.Activator())
        list.add(org.apache.felix.shell.impl.Activator())
        list.add(org.apache.felix.shell.tui.Activator())
//        list.add(org.apache.aries.blueprint.container.BlueprintExtender())
//        list.add(org.apache.felix.http.jetty.internal.JettyActivator())
        list.add(org.apache.felix.http.whiteboard.internal.WhiteboardActivator())
//        list.add(org.apache.cxf.transport.http.osgi.HTTPTransportActivator())
//        list.add(org.apache.cxf.transport.http_jetty.osgi.HTTPJettyTransportActivator())
//        list.add(org.apache.felix.webconsole.internal.OsgiManagerActivator())
//        list.add(org.apache.felix.cm.impl.Activator())
//        list.add(de.mc.ladon.s3server.osgi.S3ServerActivator())
        list.add(org.apache.felix.fileinstall.internal.FileInstall())
        config[FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP] = list
        config[BundleCache.CACHE_ROOTDIR_PROP] = getLadonHome()
        config["org.osgi.framework.storage.clean"] = "onFirstInit"
        config["felix.fileinstall.dir"] = fileInstallDir
        config["felix.fileinstall.bundles.new.start"] = true
        config["org.osgi.service.http.port"] = 8080
        felix = Felix(config)
        felix.start()
    }


    fun getInstalledBundles(): Array<out Bundle> {
        return activator.getBundles()
    }

    fun shutdownApplication() {
        felix.stop()
        felix.waitForStop(0)
    }

    companion object {
        val fileInstallDir = getSystemDir() + File.separator + "plugins"
    }

}