package de.mc.ladon.server.boot.controller.pages

import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.boot.tables.Color
import de.mc.ladon.server.boot.tables.TableCell
import de.mc.ladon.server.boot.tables.TableObject
import de.mc.ladon.server.boot.tables.TableRow
import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.config.BoxConfig
import de.mc.ladon.server.core.persistence.entities.impl.LadonContentMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonMetadata
import de.mc.ladon.server.core.persistence.entities.impl.LadonPropertyMeta
import de.mc.ladon.server.core.persistence.entities.impl.LadonResourceKey
import de.mc.ladon.server.plugin.runtime.LadonPluginRuntime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.text.SimpleDateFormat
import java.util.*

/**
 * Controller for the Plugin info page
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class PluginsPageController : FrameController() {

    @Autowired
    lateinit var pluginRuntime: LadonPluginRuntime
    @Autowired
    lateinit var metadataDAO: MetadataDAO
    @Autowired
    lateinit var binaryDataDAO: BinaryDataDAO

    private val datePattern = "yyyy-MM-dd HH:mm:ss"

    private fun fromDate(date: Date): String {
        return SimpleDateFormat(datePattern).format(date)
    }

    val stateMap = mapOf(
            1 to "UNINSTALLED",
            2 to "INSTALLED",
            4 to "RESOLVED",
            8 to "STARTING",
            10 to "STOPPING",
            20 to "ACTIVE")


    @RequestMapping(value = ["addplugin"], method = [RequestMethod.POST], consumes = ["multipart/form-data"])
    fun addPlugin(model: MutableMap<String, Any>, cc: LadonCallContext, @RequestParam(value = "file", required = false) file: MultipartFile?): String {
        if (file?.isEmpty != false) model.flashWarn("Please select a file to upload") else {
            file.inputStream.use {
                val (id, md5, length) = binaryDataDAO.saveContentStream(cc, BoxConfig.SYSTEM_REPO, it)
                val key = LadonResourceKey(BoxConfig.SYSTEM_REPO, "plugins/" + file.originalFilename, cc.getCallId().id())
                val meta = LadonMetadata()
                meta.set(LadonPropertyMeta(mutableMapOf("content-type" to file.contentType)))
                meta.set(LadonContentMeta(id, md5, length.toLong(), Date(), cc.getUser().name))
                metadataDAO.saveMetadata(cc, key, meta)
                model.flashSuccess("Upload ${file.originalFilename} success")
            }

        }
        model.addBundles()
        return super.updateModel(model, "plugins", BoxConfig.SYSTEM_REPO)
    }

    @RequestMapping("plugins")
    fun plugins(model: MutableMap<String, Any>, @RequestParam(required = false) bundleid: String?, @RequestParam(required = false) action: String?): String {
        if (bundleid != null && action == "start") {
            try {
                pluginRuntime.getInstalledBundles().find { it.bundleId == bundleid.toLong() }?.start()
            } catch (e: Exception) {
                model.flashDanger(e.message ?: "")
            }
        }


        model.addBundles()
        return super.updateModel(model, "plugins", BoxConfig.SYSTEM_REPO)
    }


    fun MutableMap<String, Any>.addBundles() {
        this["bundles"] = listOf(TableObject(
                "OSGI bundles",
                listOf("Id", "Name", "Version", "State", "Action", "Action", "Action", "LastModified"),
                pluginRuntime.getInstalledBundles().filter { it.bundleId > 0 }
                        .map {
                            TableRow(listOf(
                                    TableCell(it.bundleId.toString()),
                                    TableCell(it.symbolicName),
                                    TableCell(it.version.toString()),
                                    TableCell(it.state.let { stateMap[it] ?: it.toString() }),
                                    if (it.state == 2) TableCell("start", "plugins?bundleid=${it.bundleId}&action=start") else TableCell("start"),
                                    if (it.state == 20) TableCell("stop", "plugins?bundleid=${it.bundleId}&action=stop") else TableCell("stop"),
                                    TableCell("show file", "searchid?repoid=_system&searchpath=plugins"),
                                    TableCell(it.lastModified.let { fromDate(Date(it)) })), Color.BLUE)
                        }))
    }

}

