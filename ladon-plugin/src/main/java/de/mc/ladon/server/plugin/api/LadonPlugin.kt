package de.mc.ladon.server.plugin.api

import de.mc.ladon.server.plugin.runtime.PluginContext

interface LadonPlugin {


    fun adminPage(pluginContext: PluginContext): LadonAdminPage?

    fun install(pluginContext: PluginContext): Boolean

    fun uninstall(pluginContext: PluginContext): Boolean

    fun start(pluginContext: PluginContext): Boolean

    fun stop(pluginContext: PluginContext): Boolean


}