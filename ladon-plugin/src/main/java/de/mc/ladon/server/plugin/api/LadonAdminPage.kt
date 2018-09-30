package de.mc.ladon.server.plugin.api

interface LadonAdminPage {

    val path: String

    val title: String

    val icon: String

    val template: String

    val context: Map<String, Any>
}