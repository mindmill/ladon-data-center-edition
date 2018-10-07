package de.mc.ladon.server.plugin.api


interface LadonPlugin {


    fun adminPage(pluginContext: PluginContext): AdminPage?

    fun install(pluginContext: PluginContext): Boolean

    fun uninstall(pluginContext: PluginContext): Boolean

    fun start(pluginContext: PluginContext): Boolean

    fun stop(pluginContext: PluginContext): Boolean


}