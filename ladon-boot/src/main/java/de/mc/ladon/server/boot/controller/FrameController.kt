package de.mc.ladon.server.boot.controller

import com.datastax.driver.core.exceptions.NoHostAvailableException
import de.mc.ladon.server.core.api.persistence.entities.Repository
import de.mc.ladon.server.core.util.ByteFormat

/**
 * FrameController
 * Created by Ralf Ulrich on 12.12.15.
 */
open class FrameController {


    fun updateModel(model: MutableMap<String, Any>, path: String, repoid: String): String {
        return updateMenu(model, path, repoid)
    }


    private fun updateMenu(model: MutableMap<String, Any>, path: String, repoid: String): String {
        model["repoid"] = repoid
        try {
            model["repositories"] = getRepos(model["repoprefix"] as String? ?: "")
        } catch (e: NoHostAvailableException) {
            model.flashDanger("ALL CASSANDRA HOSTS ARE DOWN!")
        } catch (e: IllegalStateException) {
            model.flashDanger("ALL CASSANDRA HOSTS ARE DOWN!")
        }
        model["path"] = path + ".vm"
        model["menuitems"] = getMenuItems(path, repoid)
        return "templates/border"
    }

    open fun getRepos(prefix: String = "") = listOf<Repository>()

    fun getMenuItems(path: String, repoid: String): List<MenuItem> {
        return arrayListOf(
                MenuItem("overview", path, repoid, "fa fa-dashboard", "Dashboard", "green"),
                MenuItem("repositories", path, repoid, "fa fa-bitbucket", "Buckets", "green"),
                MenuItem("searchid", path, repoid, "fa fa-file", "Files", "green"),
                // .addChild(MenuItem("searchid", path, repoid, "fa fa-file", "Explorer", "green"))
                //.addChild(MenuItem("debug", path, repoid, "fa fa-file", "Details", "green")),
                MenuItem("users", path, repoid, "fa fa-user", "Users", "green"),
                //MenuItem("plugins", path, repoid, "fa fa-plug", "Plugins", "green"),
                MenuItem("system", path, repoid, "fa fa-area-chart", "System", "green"),
                MenuItem("swagger-ui.html", path, repoid, "fa fa-plug", "REST API", "green"),
                MenuItem("cassandra", path, repoid, "fa fa-database", "Cassandra", "green")
        )
    }

    class MenuItem(val path: String, val currentPath: String, val repoid: String, val icon: String, val text: String, val color: String) {
        val children: MutableList<MenuItem> = arrayListOf()

        fun addChild(child: MenuItem): MenuItem {
            children.add(child);return this
        }

        override fun toString(): String {
            return "<a ${if (currentPath.startsWith(this.path) || children.any { c -> currentPath.startsWith(c.path) }) "class=\"active-menu\"" else ""} href=\"$path?repoid=$repoid \">" +
                    "<i class=\"${icon}\"></i> $text${if (children.isNotEmpty()) "<span class=\"fa arrow\"></span>" else ""}</a>"
        }
    }


    fun MutableMap<String, Any>.flashWarn(message: String) {
        flash(message, "warning")
    }

    fun MutableMap<String, Any>.flashInfo(message: String) {
        flash(message, "info")
    }

    fun MutableMap<String, Any>.flashDanger(message: String) {
        flash(message, "danger")
    }

    fun MutableMap<String, Any>.flashSuccess(message: String) {
        flash(message, "success")
    }

    private fun MutableMap<String, Any>.flash(message: String, type: String) {
        this["flash"] = Pair(type, message)
    }

}

fun Long.humanReadable(): String {
    return ByteFormat.humanReadableByteCount(this, true)
}
