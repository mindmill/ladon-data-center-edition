package de.mc.ladon.server.plugin.runtime

import de.mc.ladon.server.core.api.persistence.Database
import de.mc.ladon.server.core.api.persistence.dao.*
import de.mc.ladon.server.core.util.PathUtils.getLadonHome
import de.mc.ladon.server.core.util.PathUtils.getSystemDir
import de.mc.ladon.server.plugin.api.LadonPlugin
import de.mc.ladon.server.plugin.api.PluginContext
import org.apache.felix.framework.Felix
import org.apache.felix.framework.cache.BundleCache
import org.apache.felix.framework.util.FelixConstants
import org.osgi.framework.Bundle
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.util.tracker.ServiceTracker
import org.osgi.util.tracker.ServiceTrackerCustomizer
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class LadonRuntimeActivator(val pluginContext: PluginContext) : BundleActivator {
    val log = LoggerFactory.getLogger(javaClass)
    private var pluginTracker: ServiceTracker<LadonPlugin, Any>? = null
    private var context: BundleContext? = null

    fun getBundles(): Array<out Bundle> = context?.bundles.orEmpty()


    override fun start(context: BundleContext) {
        log.info("start host ativator")
        this.context = context
        context.addBundleListener { log.info(it.toString()) }

        context.registerService(BinaryDataDAO::class.java, pluginContext.binaryDataDAO, null)
        context.registerService(ChangeTokenDAO::class.java, pluginContext.changeTokenDAO, null)
        context.registerService(MetadataDAO::class.java, pluginContext.metadataDAO, null)
        context.registerService(RepositoryDAO::class.java, pluginContext.repositoryDAO, null)
        context.registerService(ChunkDAO::class.java, pluginContext.chunkDAO, null)
        context.registerService(Database::class.java, pluginContext.database, null)
        context.registerService(UserRoleDAO::class.java, pluginContext.userRoleDAO, null)


        pluginTracker = ServiceTracker(context, LadonPlugin::class.java,
                object : ServiceTrackerCustomizer<LadonPlugin, Any> {
                    override fun addingService(reference: ServiceReference<LadonPlugin>): LadonPlugin? {
                        val plugin = context.getService(reference)
                        log.info("added plugin ${plugin.javaClass}")
                        return null
                    }

                    override fun modifiedService(reference: ServiceReference<LadonPlugin>, service: Any) {}

                    override fun removedService(reference: ServiceReference<LadonPlugin>, service: Any) {
                        val plugin = context.getService(reference)
                        log.info("removed plugin  ${plugin.javaClass}")

                    }

                })
        pluginTracker?.open()

    }

    override fun stop(context: BundleContext) {
        log.info("stop host activator")
        this.context = null
        pluginTracker?.close()
    }
}

@Named
class LadonPluginRuntime @Inject constructor(pluginContext: PluginContext) {

    private val activator: LadonRuntimeActivator
    private var felix: Felix

    init {
        Files.createDirectories(Paths.get(getSystemDir()))
        Runtime.getRuntime().addShutdownHook(Thread { shutdownApplication() })
        val config = HashMap<String, Any>()
        activator = LadonRuntimeActivator(pluginContext)
        val list = ArrayList<BundleActivator>()
        list.add(activator)
        list.add(org.slf4j.osgi.logservice.impl.Activator())
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