package de.mc.ladon.server.plugin.api


interface LadonPlugin {


    fun adminPage(pluginContext: PluginContext): AdminPage?


}